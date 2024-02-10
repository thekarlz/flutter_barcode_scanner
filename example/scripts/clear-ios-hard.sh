cd ..
# shellcheck disable=SC2164
cd ios
rm -rf Pods
rm Podfile.lock
# shellcheck disable=SC2103
cd ..
flutter clean
flutter pub get
# shellcheck disable=SC2164
cd ios
pod install
