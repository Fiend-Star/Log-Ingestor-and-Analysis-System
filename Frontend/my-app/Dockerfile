# Base image for building
FROM node:latest as build

# Set working directory
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json ./
COPY package-lock.json ./
RUN npm install

# Copy the rest of your app's source code from your host to your image filesystem.
COPY . ./

# Build the app
RUN npm run build

# Nginx to serve the content on port 3001
FROM nginx:latest

# Copy the built React app to the Nginx directory
COPY --from=build /app/build /usr/share/nginx/html

# Update the default Nginx configuration to listen on port 3001
RUN sed -i 's/80/5001/g' /etc/nginx/nginx.conf

EXPOSE 5001

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
