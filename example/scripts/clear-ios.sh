flutter clean
flutter pub get
cd ..
# shellcheck disable=SC2164
cd ios
pod install
# shellcheck disable=SC2103
cd ..
