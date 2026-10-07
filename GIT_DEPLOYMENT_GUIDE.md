# 🚀 Git Bash & GitHub Deployment Guide

Follow these exact steps to push this project to your GitHub account and trigger the automated CI/CD pipeline.

---

## Step 1: Open Git Bash

1. Press the `Windows Key`, type **Git Bash**, and hit `Enter`.
2. Navigate directly to the project folder:

```bash
cd "/c/Users/jakka/OneDrive/Desktop/telecom/telecom-network-monitor"
```

Verify your current directory by typing:
```bash
pwd
```
It should output: `/c/Users/jakka/OneDrive/Desktop/telecom/telecom-network-monitor`

---

## Step 2: Initialize Git Repository

Initialize the local Git repository and set the default branch to `main`:

```bash
git init
git branch -M main
```

---

## Step 3: Stage and Commit the Code

Add all project files (including Spring Boot backend, Node.js frontend, telemetry simulator, datasets, CI/CD workflows, Docker configs, and architecture documentation):

```bash
git add .
git status
```

Commit the files with an impressive, professional commit message:

```bash
git commit -m "feat: Initial commit of Telecom Network Incident Monitoring Platform with Spring Boot 3, Kafka ingestion, Redis caching, Node.js Operations Dashboard, and GitHub Actions CI/CD"
```

---

## Step 4: Create a New Repository on GitHub

1. Open your browser and go to: **[https://github.com/new](https://github.com/new)**
2. In **Repository name**, enter:
   ```
   telecom-network-monitor
   ```
3. Set visibility to **Public** (recommended for portfolio & resume sharing).
4. **IMPORTANT**: Do **NOT** check "Add a README file", "Add .gitignore", or "Choose a license" (we already have all of them locally).
5. Click the green button **"Create repository"**.

---

## Step 5: Link and Push to GitHub

Copy your repository URL from GitHub (replace `YOUR_GITHUB_USERNAME` with your actual GitHub username):

```bash
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/telecom-network-monitor.git
```

Now push your code:

```bash
git push -u origin main
```

*(If prompted, enter your GitHub credentials or Personal Access Token).*

---

## Step 6: Verify GitHub Actions CI/CD Pipeline

1. Go to your repository page on GitHub: `https://github.com/YOUR_GITHUB_USERNAME/telecom-network-monitor`
2. Click on the **"Actions"** tab at the top.
3. You will see the **"Telecom Monitor CI/CD Pipeline"** running:
   - **Job 1 (Build, Unit & Integration Tests)**: Boots up Ubuntu runner, installs JDK 21, compiles Spring Boot backend, executes all 10 unit and integration tests with Maven, and uploads Surefire test artifacts.
   - **Job 2 (Node.js Frontend Linter & Verification)**: Installs Node.js dependencies, verifies Express server syntax.
   - **Job 3 (Docker Container Image Build)**: Uses Docker Buildx to build the multi-stage production container image.
4. When all jobs finish, you will see a green checkmark (`✔`), proving that your code compiles, passes all automated tests, and containerizes successfully!

---

## Step 7: How to Make Future Updates

Whenever you make improvements or changes to the code:

```bash
cd "/c/Users/jakka/OneDrive/Desktop/telecom/telecom-network-monitor"
git add .
git commit -m "feat: your update message"
git push origin main
```
GitHub Actions will automatically run tests on every push!
