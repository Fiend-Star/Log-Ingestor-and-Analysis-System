# Start with a base image containing Node.js runtime
FROM node:18 as build

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json, then install dependencies
COPY package.json package-lock.json ./
RUN npm install

# Copy the rest of your app's source code
COPY . .

# Build the app
RUN npm run build

# Use a specific version of Nginx
FROM nginx:1.21

# Set working directory to Nginx asset directory
WORKDIR /usr/share/nginx/html

# Remove default Nginx static assets
RUN rm -rf ./*

# Copy static assets from builder stage
COPY --from=build /app/build .

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 5001

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
