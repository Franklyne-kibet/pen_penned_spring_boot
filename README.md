# Pen Penned Blogging App Backend Service - Deployment Guide

## Overview

This document provides the necessary commands to deploy the Pen Penned Blogging App in both production and
development environments using Docker Compose.

---

## Deployment Commands for Production

### 1. Initial Deployment (Without SSL)

Run the following command to start the necessary services without SSL:

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build nginx blog-app postgres redis
```

### 2. Obtain SSL Certificates

After configuring your domain's DNS settings, obtain SSL certificates using Certbot:

```bash
docker-compose -f docker-compose.prod.yml up certbot
```

### 3. Full Deployment with Load Balancing

To deploy the application with scaling for better performance and reliability, run:

```bash
docker-compose -f docker-compose.prod.yml up -d --scale blog-app=3
```

---

## Building and Deploying in Production

### 1. Build and Deploy in Production

Rebuild and deploy the production environment with PostgreSQL and the blog application:

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build postgres blog-app
```

### 2. Rebuild and Restart Production Environment

Force a clean rebuild without using cache:

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache
```

Re-deploy after the clean build:

```bash
docker-compose -f docker-compose.prod.yml up -d postgres blog-app
```

---

## Deployment Commands for Development

### 1. Build and Deploy in Development

Use the following command to build and deploy the development environment:

```bash
docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d --build postgres blog-app-dev
```

### 2. Rebuild and Restart Development Environment

Force a clean rebuild without using cache:

```bash
docker-compose -f docker-compose.dev.yml --env-file .env.dev build --no-cache
```

Re-deploy after the clean build:

```bash
docker-compose -f docker-compose.dev.yml up -d postgres blog-app-dev
```

---

## Notes:

- Ensure that `.env.prod` and `.env.dev` files are properly configured before running these commands.
- Certbot should only be run after DNS configuration to generate SSL certificates correctly.
- Scaling is only needed for production environments where multiple instances are required.
- The `--no-cache` option ensures that a fresh build is performed, avoiding stale configurations.

This guide should help you effectively manage and deploy the Pen Penned Blogging App in different environments.

