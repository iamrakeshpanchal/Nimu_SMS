#!/bin/bash

# Create project directory
mkdir -p Nimu_SMS/{app/src/main/{java/com/iamrakeshpanchal/nimusms/{data/{entities,dao},viewmodels,repositories,services,receivers,workers,ui/{bubbles}},res/{layout,values,drawable,xml}},gradle}

# Create essential files quickly
echo 'android.application' > Nimu_SMS/local.properties 2>/dev/null || echo 'sdk.dir=/path/to/sdk' > Nimu_SMS/local.properties
echo 'buildscript' > Nimu_SMS/build.gradle 2>/dev/null
echo 'include ":app"' > Nimu_SMS/settings.gradle 2>/dev/null

echo "✨ Project skeleton created at Nimu_SMS/"
echo "📁 Run the full setup script for complete configuration"
