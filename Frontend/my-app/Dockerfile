# Base image
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

# Nginx to serve the content
FROM nginx:latest
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
