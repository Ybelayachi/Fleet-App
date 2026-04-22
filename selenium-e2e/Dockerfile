FROM maven:3.9.11-eclipse-temurin-21

# Install Google Chrome
RUN apt-get update \
    && apt-get install -y wget gnupg ca-certificates \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub \
       | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] \
       http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cache Maven dependencies
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

ENV BASE_URL=http://fleet-frontend:4200
ENV E2E_API_ROOT_URL=http://fleet-backend:8080

CMD ["sh", "-c", "mvn test -De2e.headless=true -DbaseUrl=${BASE_URL} -De2e.apiRootUrl=${E2E_API_ROOT_URL}"]
