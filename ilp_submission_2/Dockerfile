#
# Build stage
#
# Use an official Maven image as the base image
FROM maven:3.9.9-amazoncorretto-21-debian AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
COPY data ./data
# Build the application using Maven
RUN mvn clean package -DskipTests
# Copy the built JAR file from the previous stage to the container
# to make things smaller and faster, we are using a distroless image
FROM gcr.io/distroless/java21-debian12
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/ilp_submission_2*.jar app.jar
# Copy the data folder to the final image
COPY --from=build /app/data ./data
EXPOSE 8080

# Showing how ENV variables can be set
# ENV ILP_SERVICE_URL=https://ilp-rest-2024.azurewebsites.net/

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
