# Import dependencies
from mcp.server.fastmcp import FastMCP
import json
import httpx
import subprocess
import uuid
import os
import folium
import webbrowser


import sys
print(">>> Using Python:", sys.executable, file=sys.stderr)


# Server created
mcp = FastMCP("Medical Drone MCP")
BASE_URL = "http://localhost:8080/api/v1"

# Create the tool
@mcp.tool()
async def drones_with_cooling(state: bool) -> dict:
    """
    Get a list of drone IDs filtered by cooling capability.
    Calls: GET /api/v1/dronesWithCooling/{state}
    
    Args:
        state: True for drones WITH cooling, False for drones WITHOUT cooling
    
    Returns:
        A list of drone IDs
    """
    url = f"{BASE_URL}/dronesWithCooling/{state}"

    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(url, timeout=10)
            response.raise_for_status()
            drones = response.json()
            return{
                "drones": drones,
                "count": len(drones)
            }
    except Exception as e:
        return {
            "count": 0,
            "drones": [],
            "error": str(e)
        }

@mcp.tool()
async def place_order(medicine: dict) -> dict:
    """
    Place a new medical delivery order.
    Calls POST /api/v1/placeOrder

    Expected `medicine` format:
    {
      "id": int,                         # required
      "date": "YYYY-MM-DD",              # required
      "time": "HH:MM",                   # required
      "requirements": {                  # required
          "capacity": float,
          "cooling": bool,
          "heating": bool,
          "maxCost": float
      },
      "delivery": {                      # required
          "lat": float,
          "lng": float
      }
    }

    Returns Spring Boot OrderResponse:
    {
      "status": "PLACED" or "FAILED",
      "message": "...",
      "droneId": "DRONE123"
    }
    """
    import httpx

    async with httpx.AsyncClient() as client:
        response = await client.post(
            "http://localhost:8080/api/v1/placeOrder",
            json=medicine
        )

    return response.json()

@mcp.tool()
async def show_flight_path(medicine: dict) -> str:
    """
    Fetch and display the flight path for a given order.
    Calls POST /api/v1/showFlightPath

    Expected input:
    {
      "medicine": {
        "id": int,
        "date": "YYYY-MM-DD",
        "time": "HH:MM",
        "requirements": {
            "capacity": float,
            "cooling": bool,
            "heating": bool,
            "maxCost": float
        },
        "delivery": {
            "lat": float,
            "lng": float
        }
      }
    }

    Returns:
        A message indicating that the flight path viewer is opening in the user's browser.
    """

    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{BASE_URL}/showFlightPath",
            json={"medicine": medicine}
        )

    geojson = response.json()

    # 2. Create unique HTML file
    filename = f"flightpath_{uuid.uuid4().hex}.html"
    filepath = os.path.abspath(filename)

    # 3. Extract route for centering
    coords = geojson["features"][0]["geometry"]["coordinates"]
    lat_center = sum(c[1] for c in coords) / len(coords)
    lng_center = sum(c[0] for c in coords) / len(coords)

    # 4. Build folium map directly here
    m = folium.Map(location=[lat_center, lng_center], zoom_start=14)
    folium.GeoJson(geojson).add_to(m)

    m.save(filepath)

    # 5. Open in browser directly (no subprocess!)
    webbrowser.open(f"file://{filepath}")

    return f"Opening drone flight path viewer ({filename})…"

@mcp.tool()
async def drones_available_for_medicine(medicine: list) -> dict:
    """
    Get a list of drone IDs available for the given medicine requirements.
    Calls POST /api/v1/queryAvailableDrones

    Tool Input (parameter `medicine`):
        A LIST of medicine objects, for example:

        [
          {
            "id": 123,
            "date": "2025-12-22",
            "time": "14:30",
            "requirements": {
                "capacity": 0.5,
                "cooling": false,
                "heating": false,
                "maxCost": 50.0
            },
            "delivery": {
                "lat": 55.9444,
                "lng": -3.19031
            }
          }
        ]

    This is sent directly as the JSON POST body.

    Returns:
        A list of drone IDs
    """
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "http://localhost:8080/api/v1/queryAvailableDrones",
                json=medicine
            )
            response.raise_for_status()
            drones = response.json()
            return {
                "drones": drones,
                "count": len(drones)
            }

    except Exception as e:
        return {
            "count": 0,
            "drones": [],
            "error": str(e)
        }

@mcp.tool()
async def get_medicine_info(name: str) -> dict:
    """
    Get requirements for a medicine in stock.

    Calls: GET /api/v1/medicineRequirement/{name}

    Input:
        name (str): The medicine name, e.g. "insulin"

    Successful response example:
    {
      "capacity": 0.4,
      "cooling": true,
      "heating": false,
      "servicePoints": [1, 2]
    }

    If medicine is not found (404), return:
    {
      "error": "NOT_FOUND",
      "message": "Medicine 'paracetamol' is not in stock."
    }

    Returns:
        dict: requirements or error message
    """
    url = f"{BASE_URL}/medicineRequirement/{name}"

    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    if response.status_code == 200:
        return response.json()

    if response.status_code == 404:
        return {
            "error": "NOT_FOUND",
            "message": f"Medicine '{name}' is not in stock."
        }

    return {
            "status": response.status_code,
            "message": "There was an error retrieving the medicine information. Try again later."
        }

@mcp.tool()
async def delivery_possible_at_location(location: dict) -> str:
    """
    Check if delivery is possible at the given location.
    Calls POST /api/v1/deliveryLocationAccessible

    Expected `location` format:
    {                      
        "lng": float                # required
        "lat": float,               # required
    }

    Backend returns:
        true  -> delivery is possible
        false -> location lies in a restricted region

    Returns:
        A human-friendly message describing whether delivery can occur.
    """
    async with httpx.AsyncClient() as client:
        response = await client.post(
            "http://localhost:8080/api/v1/deliveryLocationAccessible",
            json=location
        )
        response.raise_for_status()
        deliveryPossible = response.json()
        
        if deliveryPossible:
            return "Delivery is possible at the given location."
        else:
            return "Delivery location lies within a restricted region."

def main():
    mcp.run(transport="stdio")

if __name__ == "__main__":
    main()
