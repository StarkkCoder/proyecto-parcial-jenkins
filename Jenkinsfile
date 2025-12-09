pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven-3.9'
    }

    environment {
        APP_NAME     = "todo-api"
        DOCKER_IMAGE = "todo-api:latest"
        HEALTH_URL   = "http://todo-api:3000/actuator/health"
        MAIL_ADMIN   = "angel_cevallos99@hotmail.com"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "📥 Obteniendo código del repositorio..."
                checkout scm
            }
        }

        stage('Run Tests') {
            steps {
                echo "🧪 Ejecutando tests (unit + integration)..."
                sh 'mvn -B clean test'
            }
            post {
                always {
                    echo "📊 Publicando resultados de tests (JUnit)..."
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build JAR') {
            steps {
                echo "📦 Construyendo artefacto JAR..."
                sh 'mvn -B clean package -DskipTests'
            }
            post {
                success {
                    echo "✅ JAR generado correctamente."
                }
                failure {
                    error("❌ Falló el build del JAR.")
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "🐳 Construyendo imagen Docker..."
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                echo "🚀 Desplegando aplicación con Docker Compose..."

                // Detiene stack previo si existe (sin romper el pipeline)
                sh "docker compose down || true"

                // Levanta la nueva versión
                sh "docker compose up -d --build"
            }
        }

        stage('Health Check') {
            steps {
                echo "🔍 Verificando salud del servicio..."
                script {
                    retry(5) {
                        sleep 3
                        sh "curl -f ${HEALTH_URL}"
                    }
                }
            }
        }
    }

    post {

        success {
            echo "🎉 Pipeline completado con éxito."
            mail to: "${MAIL_ADMIN}",
                 subject: "Jenkins SUCCESS - ${APP_NAME}",
                 body: """El pipeline terminó correctamente.
Build: ${env.BUILD_NUMBER}
Job:   ${env.JOB_NAME}
URL:   ${env.BUILD_URL}"""
        }

        failure {
            echo "💥 Pipeline falló."
            mail to: "${MAIL_ADMIN}",
                 subject: "Jenkins FAILURE - ${APP_NAME}",
                 body: """El pipeline falló.
Build: ${env.BUILD_NUMBER}
Job:   ${env.JOB_NAME}
URL:   ${env.BUILD_URL}"""
        }
    }
}
