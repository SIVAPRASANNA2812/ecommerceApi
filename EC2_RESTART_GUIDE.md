# 🚀 AWS EC2 Re-Launch & Maintenance Guide

This document provides a step-by-step procedure to restart, reconnect, and run your **E-Commerce Backend API** on your AWS EC2 instance anytime after stopping it.

---

## 📌 Background: Why the IP Changes

When you stop and start an EC2 instance, AWS releases the old dynamic public IPv4 address and assigns a **new public IPv4 address** upon starting. Because of this, every time you start the instance, you will:
1. Connect using the **new IP**.
2. Update the live URL in your `README.md` with the **new IP**.

---

## 📋 Step-by-Step Re-Launch Procedure

### Step 1: Start the Instance in AWS Console
1. Log into your [AWS EC2 Console](https://console.aws.amazon.com/ec2/).
2. Go to **Instances**.
3. Select your instance (click the checkbox).
4. Click the **Instance state** dropdown button at the top right.
5. Select **Start instance**.
6. Wait ~30 seconds until the instance state turns green: **Running**.

---

### Step 2: Copy the New Public IPv4 Address
1. Click on the running instance to open the **Instance summary** panel at the bottom.
2. Find and copy the **Public IPv4 address** (e.g., `13.234.xx.xx`).

---

### Step 3: SSH into EC2 from Windows PowerShell
1. Open PowerShell on your computer.
2. Navigate to your Downloads folder where your `.pem` key is stored:
   ```powershell
   cd C:\Users\sivap\Downloads
   ```
3. Connect using SSH (replace `<NEW-PUBLIC-IP>` and `your-key.pem` with your values):
   ```powershell
   ssh -i .\your-key.pem ubuntu@<NEW-PUBLIC-IP>
   ```
   *(If prompted `Are you sure you want to continue connecting (yes/no)?`, type `yes` and hit Enter).*

---

### Step 4: Start the Docker Container
Because the Docker image and container are already built on your EC2 instance from before, **you do NOT need to rebuild anything**.

Simply run this one command inside your EC2 terminal:
```bash
sudo docker start ecommerce-app
```

Verify that it is running:
```bash
# Check container status
sudo docker ps

# Check application startup logs
sudo docker logs --tail 20 ecommerce-app
```
*(You will see: `Started EcommerceApiApplication in ... seconds`)*.

---

### 💡 Pro-Tip: Auto-Start App on Every Boot (Optional)
If you want the Docker container to **start automatically every time you power on the EC2 instance** without ever having to SSH in, run this command once on your EC2 terminal:

```bash
sudo docker update --restart unless-stopped ecommerce-app
```
*(With this set, whenever you start the instance in the AWS console, your Spring Boot backend starts automatically within 30 seconds).*

---

### Step 5: Test in Your Browser
Open your browser on your computer and test Swagger UI:

```text
http://<NEW-PUBLIC-IP>:8080/swagger-ui/index.html
```

> **Troubleshooting:** If the page does not load, ensure **Port 8080** is open in the AWS Console:
> * EC2 → Click instance → **Security** tab → Click **Security Group** → **Edit inbound rules** → Add `Custom TCP`, Port `8080`, Source `0.0.0.0/0` → Save rules.

---

### Step 6: Update `README.md` & Push to GitHub
1. Open `README.md` in your local project.
2. In lines 11 and 12, update the IP address to your new IP:
   ```markdown
   | **Live API Base URL** | `http://<NEW-PUBLIC-IP>:8080` |
   | **Interactive Swagger UI** | `http://<NEW-PUBLIC-IP>:8080/swagger-ui/index.html` |
   ```
3. Commit and push the updated documentation to GitHub:
   ```powershell
   git commit -am "docs: update live EC2 IP address in README"
   git push origin main
   ```

---

## 🛑 How to Safely Stop the Instance (To Save AWS Credits)

When your evaluation is complete or you want to pause:
1. In AWS Console → **Instances**.
2. Select your instance.
3. Click **Instance state** → **Stop instance**.
*(Do NOT click "Terminate" unless you want to permanently delete the server).*
