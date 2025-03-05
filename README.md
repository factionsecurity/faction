# OWASP - FACTION PenTesting Report Generation and Collaboration Framework

 ![GitHub last commit](https://img.shields.io/github/last-commit/factionsecurity/faction) ![GitHub Release Date - Published_At](https://img.shields.io/github/release-date/factionsecurity/faction)

[![](https://img.shields.io/badge/null0perat0r-it?style=flat-square&logo=mastodon&labelColor=white&color=white&link=https%3A%2F%2Finfosec.exchange%2F%40null0perat0r)](https://infosec.exchange/@null0perat0r)
[![Bluesky](https://img.shields.io/badge/Bluesky-0285FF?logo=bluesky&logoColor=fff)](https://bsky.app/profile/factionsecurity.com)

___Faction is now an OWASP Project! You can find more information [here](https://owasp.org/www-project-faction/)___

![image](https://github.com/factionsecurity/faction/assets/2343831/d9237bed-302f-4e6a-9716-22ae88d0dc36)

## Sponsors
### Premium Sponsors 
<table>
 <tr><td>
 <a href="https://www.otto-js.com"><img src="https://www.otto-js.com/hs-fs/hubfs/my-lovely-purple-ottot-alternate-palate-1-purple.png?width=200&height=200&name=my-lovely-purple-ottot-alternate-palate-1-purple.png"/></a><br/><a href="https://www.otto-js.com">otto-js - PCI and Client Side Security</a> 
 </td></tr>
</table>

### Become a Sponsor ❤️
If you like the project and would like to see it advance then consider being a sponser. All sponsers get access to the Faction discord server and will have bug reports priotirized. Just click the sponsor links at the top of this repo. 

# Introduction

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

__Want to see it in action?__ -> [Faction YouTube Channel](https://www.youtube.com/@factionsecurity/videos)

## Quick Setup
__Requirements__
- Java JDK11 
- Maven (for building the project)
- (Optional for VM). Mongo DB requires a CPU with AVX support. You may run into this issue if using [Oracle Virtual](https://www.mongodb.com/community/forums/t/could-not-start-mongodb-5-0-running-oracle-linux-on-virtualbox/120524/10) Box or [Kubernetes](https://stackoverflow.com/questions/70818543/mongo-db-deployment-not-working-in-kubernetes-because-processor-doesnt-have-avx)

Run the following commands to build the war file and deploy it to the docker container. 
```
git clone git@github.com:factionsecurity/faction.git
cd faction
docker-compose up --build
```

Once the containers are up you can navigate to http://127.0.0.1:8080 to access your FACTION instance. 
On the first boot, it will ask you to create an admin account. 

## Import the Vulnerability Templates
1. Navigate to Templates -> Default Vulnerabilities
2. Click Update from Faction. 

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


__Remediation/Retest Queue__
![image](https://github.com/factionsecurity/faction/assets/2343831/31a576a7-8cee-4b41-9a72-52eccec8d3d8)

__Schedule Retests__
![image](https://github.com/factionsecurity/faction/assets/2343831/421a9150-96a8-4ce8-ba06-061562012c16)

__Assessor Retest Interface__
![image](https://github.com/factionsecurity/faction/assets/2343831/11421c1f-63db-44e2-a692-a3918ddbf2ac)

__Vulnerability Status Tracking__
![image](https://github.com/factionsecurity/faction/assets/2343831/a1973dba-663c-4617-bd78-ffb08eb27973)

# 1.2 Release Updates

Faction 1.2 introduces the App Store! The Faction App Store will make it easier for developers to extend faction. Faction Extensions can be used to trigger custom code when certain events happen in your workflow like sending all vulnerbilities to Jira when the assessment is complete or update a tracking system when retests pass or fail. More information can be found in the [documentation site](https://docs.factionsecurity.com). 

### ⭐️ Jira Integration and AppStore Dashboard
![image](https://github.com/factionsecurity/faction/assets/2343831/53feb37e-cc66-401c-9ef0-e43fd4dc9f51)

Note you can reorder extensions so that updates for one can affect updates to the next. 

### ⭐️ Extensions for Custom Graphics
Extensions will also allow custom bar charts to your reports:
![image](https://github.com/factionsecurity/faction/assets/2343831/1657ed40-fdd3-4b29-afd3-b5d3aa8f78c5)

Generated report with graphics:
![image](https://github.com/factionsecurity/faction/assets/2343831/abf9b5d4-2638-411d-a57c-89fcd5819976)






