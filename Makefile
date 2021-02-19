build:
	./gradlew installDist
hub:
	./hub/build/install/hub/bin/hub --dataDir /tmp
classify_cli:
	./classify_cli/build/install/classify_cli/bin/classify_cli
vad_service: 
	cd vad_service && poetry run python src/VadServiceMain.py

all: 
	./hub/build/install/hub/bin/hub --dataDir /tmp &
	./classify_cli/build/install/classify_cli/bin/classify_cli &
	cd vad_service && poetry run python src/VadServiceMain.py &
# can bring jobs to foreground via `fg` command
