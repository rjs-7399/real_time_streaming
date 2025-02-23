## Ecommerce Data Analytics


### Commands for kafka message generation

- Run docker compose in terminal 1

```commandline
docker compose up -d
```

- Run python producer in terminal 2

```commandline
python3 producer_script.py
```

- Run broker container in terminal 3

```commandline
docker exec -it broker bash
```
```
# inside broker container
kafka-topics --list --bootstrap-server broker:29092
sales_transactions


kafka-console-consumer --topic sales_transactions --bootstrap-server localhost:9092

{"transactionId": "cd3fa541-9812-4dd7-b73b-49e6fef85e79", "productId": "product3", "productName": "headphone", "productCategory": "sports", "productPrice": 863.38, "productQuantity": 3, "productBrand": "samsung", "currency": "GBP", "customerId": "brownkristen", "transactionDate": "2025-02-21T06:40:433546+0000", "paymentMethod": "credit_card", "totalAmount": 2590.14}
{"transactionId": "e4457989-df7a-47b8-82cf-118c9c406897", "productId": "product5", "productName": "watch", "productCategory": "home", "productPrice": 785.7, "productQuantity": 2, "productBrand": "apple", "currency": "GBP", "customerId": "cwillis", "transactionDate": "2025-02-21T06:45:439499+0000", "paymentMethod": "debit_card", "totalAmount": 1571.4}

```


# Java 11 Setup Guide for Mac

This guide walks through setting up Java 11 for your Flink project on macOS.

## Installation Steps

### 1. Install Homebrew (if not already installed)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2. Install Java 11
```bash
brew tap adoptopenjdk/openjdk
brew install --cask adoptopenjdk11
```

### 3. Configure Java Environment

Add these lines to your `~/.zshrc`:
```bash
# Java 11 configuration
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH=$JAVA_HOME/bin:$PATH
```

Reload your shell configuration:
```bash
source ~/.zshrc
```

### 4. Verify Java Installation
```bash
java --version
```
This should show Java 11 as your current version.

## Project Configuration

### 5. Update pom.xml

Update the Java version in your project's `pom.xml`:

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <flink.version>1.18.0</flink.version>
    <target.java.version>11</target.java.version>
    <scala.binary.version>2.12</scala.binary.version>
    <maven.compiler.source>${target.java.version}</maven.compiler.source>
    <maven.compiler.target>${target.java.version}</maven.compiler.target>
    <log4j.version>2.17.1</log4j.version>
</properties>
```

### 6. Rebuild Project

Clean and rebuild your Maven project:
```bash
mvn clean
mvn package
```

## Troubleshooting

If you encounter issues:

1. Verify Java version:
```bash
/usr/libexec/java_home -V
```
This shows all installed Java versions.

2. Check JAVA_HOME setting:
```bash
echo $JAVA_HOME
```

3. If you have multiple Java versions, you can switch between them by modifying the version number in your JAVA_HOME export statement:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

## Notes
- Make sure to restart your IDE after changing Java versions
- If using IntelliJ, you may need to update the project SDK settings (File -> Project Structure -> Project SDK)
- The Flink version 1.18.0 is compatible with Java 11