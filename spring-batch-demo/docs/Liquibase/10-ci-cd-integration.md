# Chapter 10: CI/CD Integration

## Overview
Integrate Liquibase into your continuous integration and continuous deployment pipelines.

## 10.1 Pipeline Overview

### Typical Workflow
```
┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
│  Code   │──▶│  Build  │──▶│  Test   │──▶│ Deploy  │──▶│ Verify  │
│  Commit │   │         │   │         │   │         │   │         │
└─────────┘   └─────────┘   └─────────┘   └─────────┘
                                              │
                                             ─┘   └──────── ▼
                                        ┌─────────┐
                                        │ Monitor │
                                        └─────────┘
```

### Liquibase in CI/CD
```yaml
# Pipeline stages with Liquibase
stages:
  - name: validate
    liquibase: validate

  - name: test
    liquibase: update

  - name: deploy
    liquibase: update

  - name: rollback
    liquibase: rollback
```

## 10.2 GitHub Actions

### Basic Migration Workflow
```yaml
# .github/workflows/liquibase.yml
name: Liquibase Migration

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  LIQUIBASE_VERSION: 4.25.0

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Liquibase
        uses: actions/cache@v3
        with:
          path: ~/.m2/repository/org/liquibase
          key: ${{ runner.os }}-liquibase-${{ env.LIQUIBASE_VERSION }}
          restore-keys: |
            ${{ runner.os }}-liquibase-

      - name: Validate ChangeLogs
        run: |
          mvn liquibase:validate \
            -Dliquibase.url=${{ secrets.LIQUIBASE_URL }} \
            -Dliquibase.username=${{ secrets.LIQUIBASE_USERNAME }} \
            -Dliquibase.password=${{ secrets.LIQUIBASE_PASSWORD }}

  test-migration:
    needs: validate
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test
        ports: [5432:5432]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: |
          mvn test \
            -Dspring.datasource.url=jdbc:postgresql://localhost:5432/test \
            -Dspring.liquibase.enabled=true

  deploy-staging:
    needs: test-migration
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: staging

    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Staging
        run: |
          mvn liquibase:update \
            -Dliquibase.url=${{ secrets.STAGING_DB_URL }} \
            -Dliquibase.username=${{ secrets.STAGING_DB_USERNAME }} \
            -Dliquibase.password=${{ secrets.STAGING_DB_PASSWORD }} \
            -Dliquibase.contexts=staging

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-litePro
    if: github.ref == 'refs/heads/main'
    environment: production

    steps:
      - uses: actions/checkout@v4

      - name: Approve Deployment
        uses: trstringer/manual-approval@v1
        with:
          secret: ${{ github.token }}
          approvers: db-team

      - name: Deploy to Production
        run: |
          mvn liquibase:update \
            -Dliquibase.url=${{ secrets.PROD_DB_URL }} \
            -Dliquibase.username=${{ secrets.PROD_DB_USERNAME }} \
            -Dliquibase.password=${{ secrets.PROD_DB_PASSWORD }} \
            -Dliquibase.contexts=prod
```

### Rollback Workflow
```yaml
# .github/workflows/liquibase-rollback.yml
name: Emergency Rollback

on:
  workflow_dispatch:
    inputs:
      tag:
        description: 'Tag to rollback to'
        required: true
        default: 'v1.0.0'
      reason:
        description: 'Reason for rollback'
        required: true

jobs:
  rollback:
    runs-on: ubuntu-latest
    environment: production

    steps:
      - uses: actions/checkout@v4

      - name: Generate Rollback SQL
        id: rollback-sql
        run: |
          mvn liquibase:rollbackSQL \
            -Dliquibase.url=${{ secrets.PROD_DB_URL }} \
            -Dliquibase.username=${{ secrets.PROD_DB_USERNAME }} \
            -Dliquibase.password=${{ secrets.PROD_DB_PASSWORD }} \
            -Dliquibase.rollbackTag=${{ github.event.inputs.tag }} \
            > rollback.sql

          cat rollback.sql

      - name: Review Rollback
        uses: actions/github-script@v7
        with:
          script: |
            core.setOutput('sql', '${{ steps.rollback-sql.outputs.sql }}')

      - name: Execute Rollback
        if: github.event.inputs.confirm == 'true'
        run: |
          mvn liquibase:rollback \
            -Dliquibase.url=${{ secrets.PROD_DB_URL }} \
            -Dliquibase.username=${{ secrets.PROD_DB_USERNAME }} \
            -Dliquibase.password=${{ secrets.PROD_DB_PASSWORD }} \
            -Dliquibase.rollbackTag=${{ github.event.inputs.tag }}
```

## 10.3 GitLab CI

### Basic Configuration
```yaml
# .gitlab-ci.yml
variables:
  LIQUIBASE_VERSION: "4.25.0"
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

stages:
  - validate
  - test
  - deploy
  - rollback

liquibase:validate:
  stage: validate
  image: maven:3.8-openjdk-17
  script:
    - mvn liquibase:validate
  variables:
    LIQUIBASE_URL: $CI_DB_URL
    LIQUIBASE_USERNAME: $CI_DB_USER
    LIQUIBASE_PASSWORD: $CI_DB_PASSWORD
  only:
    changes:
      - db/changelog/**/*.yaml
      - db/changelog/**/*.xml

test:migration:
  stage: test
  image: maven:3.8-openjdk-17
  services:
    - postgres:15
  script:
    - mvn test
  variables:
    LIQUIBASE_URL: jdbc:postgresql://postgres:5432/test
    LIQUIBASE_USERNAME: postgres
    LIQUIBASE_PASSWORD: postgres
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml

deploy:staging:
  stage: deploy
  image: maven:3.8-openjdk-17
  script:
    - mvn liquibase:update
  variables:
    LIQUIBASE_URL: $STAGING_DB_URL
    LIQUIBASE_USERNAME: $STAGING_DB_USER
    LIQUIBASE_PASSWORD: $STAGING_DB_PASSWORD
  environment:
    name: staging
  only:
    - develop

deploy:production:
  stage: deploy
  image: maven:3.8-openjdk-17
  script:
    - mvn liquibase:update
  variables:
    LIQUIBASE_URL: $PROD_DB_URL
    LIQUIBASE_USERNAME: $PROD_DB_USER
    LIQUIBASE_PASSWORD: $PROD_DB_PASSWORD
  environment:
    name: production
  when: manual
  only:
    - main
```

### Rollback Pipeline
```yaml
rollback:
  stage: rollback
  image: maven:3.8-openjdk-17
  script:
    - |
      if [ "$ROLLBACK_METHOD" == "tag" ]; then
        mvn liquibase:rollback \
          -Dliquibase.url=$PROD_DB_URL \
          -Dliquibase.username=$PROD_DB_USER \
          -Dliquibase.password=$PROD_DB_PASSWORD \
          -Dliquibase.rollbackTag=$ROLLBACK_TAG
      elif [ "$ROLLBACK_METHOD" == "count" ]; then
        mvn liquibase:rollback \
          -Dliquibase.url=$PROD_DB_URL \
          -Dliquibase.username=$PROD_DB_USER \
          -Dliquibase.password=$PROD_DB_PASSWORD \
          -Dliquibase.rollbackCount=$ROLLBACK_COUNT
      fi
  environment:
    name: production
    action: rollback
  when: manual
  only:
    - main
```

## 10.4 Jenkins Pipeline

### Declarative Pipeline
```groovy
// Jenkinsfile
pipeline {
    agent any

    environment {
        LIQUIBASE_VERSION = '4.25.0'
    }

    stages {
        stage('Validate') {
            steps {
                script {
                    mvn "liquibase:validate -Dliquibase.url=${DB_URL} -Dliquibase.username=${DB_USER} -Dliquibase.password=${DB_PASSWORD}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    maven "test -Dspring.liquibase.enabled=true"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    mvn "liquibase:update -Dliquibase.url=${STAGING_DB_URL} -Dliquibase.username=${STAGING_DB_USER} -Dliquibase.password=${STAGING_DB_PASSWORD} -Dliquibase.contexts=staging"
                }
            }
        }

        stage('Deploy Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    timeout(time: 30, unit: 'MINUTES') {
                        input message: 'Deploy to production?'
                    }
                    mvn "liquibase:update -Dliquibase.url=${PROD_DB_URL} -Dliquibase.username=${PROD_DB_USER} -Dliquibase.password=${PROD_DB_PASSWORD} -Dliquibase.contexts=prod"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed!'
        }
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
    }
}
```

### Rollback Stage
```groovy
pipeline {
    agent any

    parameters {
        choice(name: 'ROLLBACK_TYPE', choices: ['tag', 'count', 'date'], description: 'Rollback method')
        string(name: 'ROLLBACK_VALUE', description: 'Tag name, count, or date')
    }

    stages {
        stage('Generate Rollback SQL') {
            steps {
                script {
                    if (params.ROLLBACK_TYPE == 'tag') {
                        sh "mvn liquibase:rollbackSQL -Dliquibase.url=${PROD_DB_URL} -Dliquibase.username=${PROD_DB_USER} -Dliquibase.password=${PROD_DB_PASSWORD} -Dliquibase.rollbackTag=${params.ROLLBACK_VALUE} > rollback.sql"
                    } else if (params.ROLLBACK_TYPE == 'count') {
                        sh "mvn liquibase:rollbackSQL -Dliquibase.url=${PROD_DB_URL} -Dliquibase.username=${PROD_DB_USER} -Dliquibase.password=${PROD_DB_PASSWORD} -Dliquibase.rollbackCount=${params.ROLLBACK_VALUE} > rollback.sql"
                    }
                }
            }
        }

        stage('Review Rollback') {
            steps {
                script {
                    echo 'Review rollback SQL:'
                    sh 'cat rollback.sql'
                    timeout(time: 10, unit: 'MINUTES') {
                        input message: 'Approve rollback?'
                    }
                }
            }
        }

        stage('Execute Rollback') {
            steps {
                script {
                    if (params.ROLLBACK_TYPE == 'tag') {
                        sh "mvn liquibase:rollback -Dliquibase.url=${PROD_DB_URL} -Dliquibase.username=${PROD_DB_USER} -Dliquibase.password=${PROD_DB_PASSWORD} -Dliquibase.rollbackTag=${params.ROLLBACK_VALUE}"
                    } else if (params.ROLLBACK_TYPE == 'count') {
                        sh "mvn liquibase:rollback -Dliquibase.url=${PROD_DB_URL} -Dliquibase.username=${PROD_DB_USER} -Dliquibase.password=${PROD_DB_PASSWORD} -Dliquibase.rollbackCount=${params.ROLLBACK_VALUE}"
                    }
                }
            }
        }
    }
}
```

## 10.5 Azure DevOps

```yaml
# azure-pipelines.yml
trigger:
  - main
  - develop

variables:
  - group: database-credentials
  - name: liquibaseVersion
    value: '4.25.0'

stages:
  - stage: Validate
    jobs:
      - job: ValidateChangeLogs
        pool:
          vmImage: ubuntu-latest
        steps:
          - task: Bash@3
            inputs:
              targetType: 'inline'
              script: |
                mvn liquibase:validate \
                  -Dliquibase.url=$(DB_URL) \
                  -Dliquibase.username=$(DB_USER) \
                  -Dliquibase.password=$(DB_PASSWORD)

  - stage: Test
    jobs:
      - job: RunIntegrationTests
        pool:
          vmImage: ubuntu-latest
        services:
          postgres:
            container: postgres:15
        steps:
          - task: Maven@3
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'test'
              options: '-Dspring.liquibase.enabled=true'

  - stage: DeployStaging
    dependsOn: Test
    jobs:
      - deployment: DeployToStaging
        environment: staging
        pool:
          vmImage: ubuntu-latest
        strategy:
          runOnce:
            deploy:
              steps:
                - task: Bash@3
                  inputs:
                    targetType: 'inline'
                    script: |
                      mvn liquibase:update \
                        -Dliquibase.url=$(STAGING_DB_URL) \
                        -Dliquibase.username=$(STAGING_DB_USER) \
                        -Dliquibase.password=$(STAGING_DB_PASSWORD) \
                        -Dliquibase.contexts=staging

  - stage: DeployProduction
    dependsOn: DeployStaging
    jobs:
      - deployment: DeployToProduction
        environment: production
        pool:
          vmImage: ubuntu-latest
        strategy:
          runOnce:
            deploy:
              steps:
                - task: Bash@3
                  inputs:
                    targetType: 'inline'
                    script: |
                      mvn liquibase:update \
                        -Dliquibase.url=$(PROD_DB_URL) \
                        -Dliquibase.username=$(PROD_DB_USER) \
                        -Dliquibase.password=$(PROD_DB_PASSWORD) \
                        -Dliquibase.contexts=prod
```

## 10.6 Docker-based Deployment

### Liquibase Docker Image
```dockerfile
# liquibase.Dockerfile
FROM maven:3.8-openjdk-17-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM liquibase/liquibase:4.25.0

COPY --from=builder /app/target/*.jar /liquibase/changelog/
COPY db/changelog /liquibase/changelog/db/changelog

WORKDIR /liquibase

CMD ["update"]
```

### Kubernetes Deployment
```yaml
# k8s-liquibase-job.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: liquibase:
  ttlSecondsAfterFinished:-migration
spec 300
  template:
    spec:
      serviceAccountName: liquibase-sa
      containers:
        - name: liquibase
          image: my-registry/liquibase:4.25.0
          env:
            - name: LIQUIBASE_URL
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: url
            - name: LIQUIBASE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: username
            - name: LIQUIBASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: password
          command:
            - /liquibase/latest/liquibase
            - --changeLogFile
            - /liquibase/changelog/db/changelog/master.yaml
            - --url
            - $(LIQUIBASE_URL)
            - --username
            - $(LIQUIBASE_USERNAME)
            - --password
            - $(LIQUIBASE_PASSWORD)
            - update
      restartPolicy: OnFailure
```

### Helm Deployment
```yaml
# helm/liquibase/values.yaml
replicaCount: 1

image:
  repository: my-registry/liquibase
  tag: "4.25.0"

env:
  - name: LIQUIBASE_URL
    valueFrom:
      secretKeyRef:
        name: db-secrets
        key: url
  - name: LIQUIBASE_USERNAME
    valueFrom:
      secretKeyRef:
        name: db-secrets
        key: username
  - name: LIQUIBASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-secrets
        key: password

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 100m
    memory: 256Mi

nodeSelector: {}

tolerations: []

affinity: {}
```

## 10.7 Blue-Green Deployment

### Strategy
```yaml
# Blue-Green Deployment
# Blue = Current version
# Green = New version

# 1. Deploy to Green (with new migrations)
# 2. Run Liquibase on Green database
# 3. Verify Green is healthy
# 4. Switch traffic
# 5. Keep Blue as rollback option
```

### Implementation
```bash
#!/bin/bash
# blue-green-deploy.sh

VERSION=$1
ENV=$2

# Deploy to green environment
echo "Deploying to green environment..."
kubectl apply -f k8s/green-deployment.yaml

# Run migrations on green
echo "Running migrations on green..."
kubectl exec -it green-pod -- \
    liquibase update \
    --url=$GREEN_DB_URL \
    --username=$GREEN_DB_USER \
    --password=$GREEN_DB_PASSWORD

# Health check
echo "Performing health check..."
if kubectl exec green-pod -- curl -f http://localhost:8080/health; then
    echo "Green environment is healthy"

    # Switch traffic (canary, gradual, or instant)
    echo "Switching traffic..."
    kubectl apply -f k8s/traffic-switch.yaml

    echo "Deployment complete"
else
    echo "Health check failed - rolling back"
    kubectl delete -f k8s/green-deployment.yaml
    exit 1
fi
```

## 10.8 Monitoring Deployments

### Deployment Metrics
```yaml
# Prometheus metrics for Liquibase
- name: liquibase_migrations_total
  type: counter
  help: Total number of migrations executed

- name: liquibase_migrations_failed_total
  type: counter
  help: Total number of failed migrations

- name: liquibase_migration_duration_seconds
  type: histogram
  help: Duration of migrations in seconds

- name: liquibase_pending_migrations
  type: gauge
  help: Number of pending migrations
```

### Alerts
```yaml
# Alert rules
groups:
  - name: liquibase
    rules:
      - alert: MigrationFailed
        expr: increase(liquibase_migrations_failed_total[5m]) > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Liquibase migration failed"
          description: "A Liquibase migration has failed"

      - alert: MigrationDurationHigh
        expr: liquibase_migration_duration_seconds > 300
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Migration taking too long"
          description: "Migration duration exceeds 5 minutes"

      - alert: PendingMigrations
        expr: liquibase_pending_migrations > 0
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Pending migrations detected"
          description: "{{ $value }} migrations are pending"
```

## 10.9 Security in CI/CD

### Secrets Management
```yaml
# GitHub Secrets
secrets:
  LIQUIBASE_URL: ${{ secrets.PROD_DB_URL }}
  LIQUIBASE_USERNAME: ${{ secrets.PROD_DB_USER }}
  LIQUIBASE_PASSWORD: ${{ secrets.PROD_DB_PASSWORD }}

# Azure Key Vault
variables:
  - group: database-secrets
```

### Approval Gates
```yaml
# Production deployment requires approval
environment:
  name: production
  url: https://prod.example.com
  onStop: rollback_job
  deploymentReview:
    type: production
    timeout: 30 days
```

### Audit Trail
```yaml
# Log all database changes
steps:
  - name: Log deployment
    run: |
      curl -X POST $AUDIT_ENDPOINT \
        -H "Authorization: Bearer $AUDIT_TOKEN" \
        -d "{
          'action': 'deploy',
          'environment': 'production',
          'user': '${{ github.actor }}',
          'changes': '${{ github.sha }}',
          'timestamp': '$(date -u +%Y-%m-%dT%H:%M:%SZ)'
        }"
```

## 10.10 Best Practices Summary

```markdown
## CI/CD Best Practices

### Pre-deployment
- [ ] Validate ChangeLogs in CI
- [ ] Test migrations in staging
- [ ] Generate rollback SQL for review
- [ ] Take database backup
- [ ] Notify team of deployment

### Deployment
- [ ] Run during maintenance window
- [ ] Use database connection pooling
- [ ] Monitor migration progress
- [ ] Set appropriate timeouts
- [ ] Enable detailed logging

### Post-deployment
- [ ] Verify application health
- [ ] Check monitoring dashboards
- [ ] Confirm no errors in logs
- [ ] Update documentation
- [ ] Archive deployment artifacts

### Rollback
- [ ] Test rollback procedure regularly
- [ ] Document rollback steps
- [ ] Practice rollback scenarios
- [ ] Keep rollback scripts updated
```

## Summary

| Platform | Use Case | Features |
|----------|----------|----------|
| GitHub Actions | Cloud CI/CD | Actions, secrets, environments |
| GitLab CI | Self-hosted CI | Pipelines, environments |
| Jenkins | Enterprise CI | Flexible, plugins |
| Azure DevOps | Microsoft ecosystem | YAML pipelines |
| Kubernetes | Container deployments | Jobs, Helm |
| Docker | Containerization | Reproducible builds |

## Learning Path Complete

Congratulations! You have completed the Liquibase learning path:

1. **Chapter 1**: Quick Start - Basic setup and first migration
2. **Chapter 2**: Core Concepts - ChangeLogs, ChangeSets, contexts
3. **Chapter 3**: ChangeSet Management - Organization, best practices
4. **Chapter 4**: Change Types - Complete reference
5. **Chapter 5**: Rollback Strategies - Safe rollback procedures
6. **Chapter 6**: Advanced Features - Custom changes, extensions
7. **Chapter 7**: Spring Boot Integration - Auto-configuration
8. **Chapter 8**: Production Best Practices - Security, monitoring
9. **Chapter 9**: Testing Strategies - Comprehensive testing
10. **Chapter 10**: CI/CD Integration - Pipeline automation

### Next Steps
- Practice with a real project
- Explore the [official documentation](https://docs.liquibase.com/)
- Join the [Liquibase community](https://forum.liquibase.org/)
- Consider Liquibase Pro for enterprise features
