flutter clean
flutter build apk --release --no-tree-shake-icons
cd ..
# shellcheck disable=SC2164
cd "$(pwd)/build/app/outputs/flutter-apk/"
# shellcheck disable=SC2034
app_name="Tripy"
# shellcheck disable=SC2034
prod="PROD"
mv app-release.apk "${app_name} ${prod}.apk"
open .