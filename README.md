# FACTION PenTesting Report Generation and Collaboration Framework
![image](https://github.com/factionsecurity/faction/assets/2343831/d9237bed-302f-4e6a-9716-22ae88d0dc36)


FACTION is your entire assessment workflow in a box. With FACTION you can:
1. Automate pen testing and security assessment Reports
1. Peer review and track changes for reports
1. Create customized DOCX templates for different assessment types and retests
3. Real-time collaboration with assessors via the web app and [Burp Suite Extensions](https://github.com/factionsecurity/Faction-Burp)
4. Customizable vulnerability templates with over 75 prepopulated
5. Easily manage assessment teams and track progress across your organization
6. Track vulnerability remediation efforts with custom SLA warnings and alerts  
7. Full Rest API to integrate with other tools                     

Other Features:           
1. LDAP Integration       
1. OAUTH2.0 Integration
1. SMTP integration 
1. Extendable with Custom Plugins similar to Burp Extender.
2. Custom Report Variables

__Want to see it in action?__ -> [Faction Video Overview](https://www.youtube.com/watch?v=cYi71oofUdU)

## Quick Setup
__Requirements__
- Java JDK11 
- Maven (for building the project)

Run the following commands to build the war file and deploy it to the docker container. 
```
git clone git@github.com:factionsecurity/faction.git
cd faction
mvn clean compile war:war
docker-compose up --build
```

Once the containers are up you can navigate to http://127.0.0.1:8080 to access your FACTION instance. 
On the first boot, it will ask you to create an admin account. 

## Import the Vulnerability Templates
1. Navigate to Templates -> Default Vulnerabilities
2. Click Update from VulnDB. 

## Customize reports
You can find out more information about creating your own custom report templates here:
[Custom Security Report Templates - Faction Security](https://docs.factionsecurity.com/Custom%20Security%20Report%20Templates/)

## Burp Suite Extension
[Burp Suite Extensions](https://github.com/factionsecurity/Faction-Burp)

## Manuals and Tutorials
[Manual](https://docs.factionsecurity.com/)

## Don't want to host it yourself?
We can provide hosting for your instance. All instances are single tenants so you don't have to worry about sharing infrastructure with untrusted parties. Navigate to [https://www.factionsecurity.com to learn more](https://www.factionsecurity.com). 

## Screenshots
__Vulnerability Templates__
![image](https://github.com/factionsecurity/faction/assets/2343831/b6fa6a0b-34a9-46cf-87cb-6aeb2b5d3347)

__Assessment Scheduling__
![image](https://github.com/factionsecurity/faction/assets/2343831/7410f74e-3854-41e9-843f-7ca44d79cc54)


__Peer Review and Track Changes__
![image](https://github.com/factionsecurity/faction/assets/2343831/fa72a72b-2c95-4c2c-bad1-5b34aab7fd13)


