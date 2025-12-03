# Script to fix MongoDB corruption issues
# Run this script if MongoDB containers keep restarting due to database corruption

Write-Host "=== MongoDB Corruption Fix Script ===" -ForegroundColor Yellow
Write-Host ""

# Stop containers first
Write-Host "Stopping MongoDB containers..." -ForegroundColor Cyan
docker-compose stop core-mongo-warm core-mongo-cold

Write-Host ""
Write-Host "WARNING: This will DELETE all MongoDB data!" -ForegroundColor Red
Write-Host "Data directories to be removed:" -ForegroundColor Yellow
Write-Host "  - ./data/warm" -ForegroundColor Yellow
Write-Host "  - ./data/cold" -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "Do you want to continue? (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Operation cancelled." -ForegroundColor Yellow
    exit
}

# Remove corrupted data directories
Write-Host ""
Write-Host "Removing corrupted data directories..." -ForegroundColor Cyan
if (Test-Path "./data/warm") {
    Remove-Item -Recurse -Force "./data/warm"
    Write-Host "  ✓ Removed ./data/warm" -ForegroundColor Green
}
if (Test-Path "./data/cold") {
    Remove-Item -Recurse -Force "./data/cold"
    Write-Host "  ✓ Removed ./data/cold" -ForegroundColor Green
}

# Create fresh directories
Write-Host ""
Write-Host "Creating fresh data directories..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path "./data/warm" | Out-Null
New-Item -ItemType Directory -Force -Path "./data/cold" | Out-Null
Write-Host "  ✓ Created ./data/warm" -ForegroundColor Green
Write-Host "  ✓ Created ./data/cold" -ForegroundColor Green

Write-Host ""
Write-Host "=== Fix completed! ===" -ForegroundColor Green
Write-Host "You can now start MongoDB containers with: docker-compose up -d core-mongo-warm core-mongo-cold" -ForegroundColor Cyan

