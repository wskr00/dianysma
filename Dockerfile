FROM --platform=linux/arm64 ghcr.io/graalvm/native-image-community:25i4-25.0.4.1.1-ol9-20260922 AS native-build

WORKDIR /workspace

ENV GRADLE_USER_HOME=/tmp/gradle-user-home

COPY --chmod=0755 gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src/main ./src/main

RUN --mount=type=cache,target=/tmp/gradle-user-home \
	./gradlew --no-daemon --build-cache nativeCompile && \
	cp -L /usr/lib64/libz.so.1 /tmp/libz.so.1

FROM debian:trixie-slim AS runtime

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        bash \
        iproute2 \
        tcpdump \
        libcap2-bin \
        procps \
        strace \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=native-build --chmod=0555 \
    /workspace/build/native/nativeCompile/dianysma \
    /app/dianysma

COPY --from=native-build --chmod=0444 \
    /tmp/libz.so.1 /usr/lib/libz.so.1

EXPOSE 8000

USER 65532:65532

ENTRYPOINT ["/app/dianysma"]
