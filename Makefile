APP_NAME := coda-demo
PORT := 8080
IMAGE := $(APP_NAME):latest

.PHONY: run
run:
	mvn spring-boot:run

.PHONY: test
test:
	mvn -q -DskipITs test

.PHONY: build
build:
	mvn -q -DskipTests package

.PHONY: docker-build
docker-build:
	docker build -t $(IMAGE) .

.PHONY: docker-run
docker-run:
	docker run --rm -p $(PORT):8080 $(IMAGE)

.PHONY: compose-up
compose-up:
	docker compose up --build

.PHONY: compose-down
compose-down:
	docker compose down

.PHONY: clean
clean:
	mvn -q clean
