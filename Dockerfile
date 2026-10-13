FROM --platform=linux/arm64 ghcr.io/graalvm/native-image-community:25i4-25.0.4.1.1-ol9-20260922 AS native-build

WORKDIR /workspace

ENV GRADLE_USER_HOME=/tmp/gradle-user-home

COPY --chmod=0755 gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src/main ./src/main

RUN --mount=type=cache,target=/tmp/gradle-user-home \
	./gradlew --no-daemon --build-cache nativeCompile

FROM --platform=linux/arm64 dhi.io/static:20250419-glibc-debian13 AS runtime

WORKDIR /app

COPY --from=native-build --chown=65532:65532 --chmod=0555 \
	/workspace/build/native/nativeCompile/dianysma /app/dianysma

EXPOSE 8000

USER 65532:65532

ENTRYPOINT ["/app/dianysma"]
