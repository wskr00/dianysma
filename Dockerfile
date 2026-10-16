# syntax=docker/dockerfile:1

FROM --platform=$BUILDPLATFORM golang:1.27.2-trixie AS build

WORKDIR /workspace

COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

COPY main.go ./main.go
COPY src ./src

ARG TARGETOS
ARG TARGETARCH
ARG APP_VERSION
RUN --mount=type=cache,target=/go/pkg/mod \
	--mount=type=cache,target=/root/.cache/go-build \
	test -n "$APP_VERSION" && \
	CGO_ENABLED=0 GOOS=$TARGETOS GOARCH=$TARGETARCH \
	go build -trimpath -ldflags="-s -w -X main.version=$APP_VERSION" -o /out/dianysma .

FROM dhi.io/static:20250419-debian13 AS runtime

WORKDIR /app

COPY --from=build --chmod=0555 /out/dianysma /app/dianysma

EXPOSE 8000
USER 65532:65532
ENTRYPOINT ["/app/dianysma"]
