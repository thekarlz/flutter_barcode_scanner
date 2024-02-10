flutter clean

flutter build appbundle --release --no-tree-shake-icons

cd ..

# shellcheck disable=SC2164
cd "$(pwd)/build/app/outputs/bundle/release/"

# shellcheck disable=SC2034
current_date=$(date "+%d %b")

# shellcheck disable=SC2034
app_name="Tripy"

# shellcheck disable=SC2034
mv app-release.aab "${app_name} ${current_date}.aab"

open .
