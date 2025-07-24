#!/bin/bash

echo "🔍 Checking Appwrite Docker Configuration"
echo "========================================="

echo -e "\n1. Checking _APP_FUNCTIONS_RUNTIMES:"
docker exec appwrite cat /storage/config/.env | grep _APP_FUNCTIONS_RUNTIMES

echo -e "\n2. Checking Function-related environment variables:"
docker exec appwrite cat /storage/config/.env | grep -E "_APP_FUNCTIONS|_APP_EXECUTOR"

echo -e "\n3. Checking Executor logs:"
echo "Recent executor logs:"
docker logs openruntimes-executor --tail 20

echo -e "\n4. Checking Worker Functions logs:"
echo "Recent worker-functions logs:"
docker logs appwrite-worker-functions --tail 20

echo -e "\n5. Checking running function container:"
echo "Function container status:"
docker ps | grep exc1-snackrack2

echo -e "\n6. Checking function container logs:"
CONTAINER_ID=$(docker ps -q -f name=exc1-snackrack2)
if [ ! -z "$CONTAINER_ID" ]; then
    echo "Container logs:"
    docker logs $CONTAINER_ID --tail 20
else
    echo "No running function container found"
fi