#!/bin/bash
export B="$HOME/.trae-cn/skills/gstack/browse/dist/browse.exe"

echo "=== QA Test: Business Logging System ==="

echo "--- Test 1: Products API (GET) ---"
$B goto http://localhost:8080/api/v1/products
$B text

echo "--- Test 2: Console Errors ---"
$B console --errors

echo "--- Test 3: Network Requests ---"
$B network --clear

echo "--- Test 4: Auth Login (POST) ---"
$B goto http://localhost:8080/api/v1/auth/login
$B console --errors

echo "--- Test 5: 404 Page ---"
$B goto http://localhost:8080/api/v1/nonexistent
$B text
$B console --errors

echo "--- Test 6: Access Log Check ---"
$B network

echo "=== QA Complete ==="
