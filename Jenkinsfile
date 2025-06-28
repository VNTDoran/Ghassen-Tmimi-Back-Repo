pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
        DOCKER_IMAGE_NAME = 'back'
        DOCKER_IMAGE_TAG = 'latest'
        DOCKER_REGISTRY = 'localhost:5000'
        FULL_IMAGE = "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
        NEXUS_CREDENTIALS_ID = 'nexus-creds'
        NEXUS_URL = 'http://nexusmain:8081'
    }

    stages {
        stage('📦 Checkout Source Code') {
            steps {
                echo "🔄 Checking out the latest source code..."
                checkout scm
            }
        }

        stage('🧼 Clean Previous Build') {
            steps {
                echo "🧹 Removing previous Maven build artifacts..."
                sh 'rm -rf target || true'
            }
        }

        stage('⚙️ Build & Test') {
            steps {
                echo "🛠️ Running Maven build and unit tests..."
                sh 'mvn clean package'
            }
        }

        stage('🔍 SonarQube Analysis') {
            steps {
                echo "🧪 Running SonarQube analysis..."
                withSonarQubeEnv("${env.SONARQUBE}") {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=backend'
                }
            }
        }

        stage('🔌 Debug Nexus Connection') {
            steps {
                echo "🧪 Testing Nexus connection and repository accessibility..."
                sh '''
                    echo "🔗 Checking Nexus status..."
                    curl -I ${NEXUS_URL}/service/rest/v1/status || echo "❌ Nexus connection failed"

                    echo "📦 Checking snapshots repo..."
                    curl -I ${NEXUS_URL}/repository/maven-snapshots/ || echo "⚠️ Snapshots repository not accessible"
                '''
            }
        }

        stage('🚀 Deploy JAR to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: NEXUS_CREDENTIALS_ID,
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    echo "🔐 Authenticating with Nexus..."
                    sh '''
                        curl -u ${NEXUS_USER}:${NEXUS_PASS} -I ${NEXUS_URL}/service/rest/v1/status
                        if [ $? -ne 0 ]; then
                            echo "❌ Authentication failed!"
                            exit 1
                        fi
                        echo "✅ Authentication successful"
                    '''

                    echo "📝 Writing temporary Maven settings.xml..."
                    writeFile file: 'settings.xml', text: """
<settings>
  <servers>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
                    """

                    echo "📤 Deploying JAR to Nexus..."
                    sh """
                        mvn deploy -DskipTests --settings settings.xml -X \
                          -DaltDeploymentRepository=nexus-snapshots::default::${NEXUS_URL}/repository/maven-snapshots/ \
                          -DaltReleaseDeploymentRepository=nexus-releases::default::${NEXUS_URL}/repository/maven-releases/
                    """
                }
            }
        }

        stage('🐳 Build Docker Image') {
            steps {
                echo "🔨 Building Docker image: ${FULL_IMAGE}..."
                sh """
                    docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} .
                    docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${FULL_IMAGE}
                """
            }
        }

        stage('📦 Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: NEXUS_CREDENTIALS_ID,
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh """
                        echo "🔐 Logging into Docker registry..."
                        echo "\${NEXUS_PASS}" | docker login ${DOCKER_REGISTRY} -u \${NEXUS_USER} --password-stdin

                        echo "📤 Pushing Docker image to registry..."
                        docker push ${FULL_IMAGE} || (sleep 5 && docker push ${FULL_IMAGE}) || (sleep 10 && docker push ${FULL_IMAGE})

                        echo "🚪 Logging out from Docker registry..."
                        docker logout ${DOCKER_REGISTRY}
                    """
                }
            }
        }
    }

    post {
        always {
            echo "🧽 Cleaning up Docker images and temp files..."
            sh """
                docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} || true
                docker rmi ${FULL_IMAGE} || true
                rm -f settings.xml || true
            """
        }
    }
}
