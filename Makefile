.PHONY: build hub classify_cli vad_service transcribe all

build:
	./gradlew installDist
hub: build
	./hub/build/install/hub/bin/hub --dataDir /tmp
classify_cli: build
	./classify_cli/build/install/classify_cli/bin/classify_cli
vad_service: build
	cd vad_service && poetry run python src/VadServiceMain.py
transcribe: build
	./transcribe/build/install/transcribe/bin/transcribe
all: build
	./hub/build/install/hub/bin/hub --dataDir /tmp &
	./transcribe/build/install/transcribe/bin/transcribe
	cd vad_service && poetry run python src/VadServiceMain.py &
# can bring jobs to foreground via `fg` command
