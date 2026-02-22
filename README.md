# One-Stop Medical Hub

## Description
This project implements an MCP (Model Context Protocol) server that connects Claude - an LLM application to the medical drone delivery REST service built in the previous coursework. Through this integration, users can interact with the delivery system conversationally to access information such as medicine requirements, stock availability, and delivery options, and they can also visualise a flight path for a potential delivery. Users can even place orders through a single prompt, supported by a lightweight persistence layer in the REST service to showcase how Claude streamlines order recording. Overall, the MCP server acts as an interface layer, translating natural-language queries from users into relevant API calls.

## Set-up/Install
We will be using the 
[MCP Python SDK](https://github.com/modelcontextprotocol/python-sdk?tab=readme-ov-file) with Claude. To get this running, we need to install:
- A package manager called uv - [instructions to install uv](https://docs.astral.sh/uv/getting-started/installation/#installation-methods).
- Claude desktop - [instructions to install Claude desktop](https://claude.com/download)

To run the MCP server, open `ilp_submission_3/drone` directory in an IDE of your choice and run:

- `uv venv` - create a virtual environment
- `source .venv/bin/activate` - activate the virtual environment
- `uv sync` - install all the required dependencies
- `uv run mcp install main.py` - adds the server to claude_desktop_config.json

Open Claude desktop, and go to File>Settings>Developer>Edit Config. In the folder that opens, locate and open `claude_desktop_config.json`, and replace the contents with the following:

```
{
  "mcpServers": {
    "Medical Drone MCP": {
      "command": "C:\\Users\\<your_name>\\.local\\bin\\uv.EXE",
      "args": [
        "--directory",
        <Path to the ilp_submission_3/drone directory>,
        "run",
        "--with",
        "mcp[cli]",
        "mcp",
        "run",
        "main.py"
      ]
    }
  }
}
```

Save the changes, close Claude Desktop as well as shut down any remaining Claude processes through Task Manager. Reopen Claude desktop where you should now see Medical Drone MCP under the Tools menu in the chat bar. The MCP server is fully connected!

Next, we need to run our REST service. In a fresh terminal, navigate to `ilp_submission_3/ilp_submission_2/` and run:

- `docker load -i ilp_cw3_image.tar` - load the image
- `docker run -p 8080:8080 ilp_cw3_image` - run the API in a container, exposing port 8080

You are now all setup to start chatting with Claude about the medical drone delivery system. Ask away!

## Features

You can ask Claude:

- about the requirements for a medicine.
- whether delivery by the drone service is possible at a certain location.
- which drones can deliver a medicine to you.
- to show you the path a drone may take to deliver your order.
- which drones have cooling capability
- to place an order for you.

### Note:

During the conversation, Claude will ask you if it can access a certain MCP tool - select yes.

## Example Prompts

- What are the requirements for insulin?
- Is delivery possible at 55.94523, –3.18730 or do I not have access to this medical drone delivery system?
- Tell me the drones available to deliver insulin with ID 1, delivery date 2025-01-02 at 12:00, and must be delivered to (55.941, -3.280).
- Can you show the flight path for this order? It's order 123, delivery time 14:30 on 22/12/2025, capacity 0.5, max cost 50, going to lat 55.94440 and lng -3.19031?
- Which drones have cooling capability?
- Place an order for me to deliver insulin with ID 1, delivery date 2025-01-02 at 12:00, and must be delivered to (55.941, -3.280). (To see the persistence layer in action, you can run `docker cp <containerId>:/app/data/drone_orders.db .` in a directory where you want to store this database. Open it in an IDE like VS Code or equivalent. In VS Code, you may need to install the SQLite Viewer extension if you don’t have it already. You will see the orders you have placed here.)

### Note:

This implementation is just a proof of concept, and so the MCP server has a lot of space to improve, following which simpler and cleaner prompts than the ones above will be sufficient.
