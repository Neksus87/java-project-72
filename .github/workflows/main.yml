name: Java CI

on:
  - push
  - pull_request

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Build with Gradle
        uses: gradle/gradle-build-action@v3
        with:
          gradle-version: 8.8

      - run: make setup
      - run: make lint
      - run: make test

      - name: Generate JaCoCo Test Report
        run: ./gradlew jacocoTestReport

      - name: Publish code coverage
        uses: paambaati/codeclimate-action@v5.0.0
        env:
          CC_TEST_REPORTER_ID: ${{ secrets.CC_TEST_REPORTER_ID }}
          JACOCO_SOURCE_PATH: app/src/main/java
        with:
          coverageCommand: make report
          coverageLocations: ${{ github.workspace }}/app/build/reports/jacoco/test/jacocoTestReport.xml:jacoco
