# BMDS Scala Seed
---

## Features

* [Play 2.5.4](https://www.playframework.com/documentation/2.5.x/Home)
* [Slick 3.1 FRM](https://www.playframework.com/documentation/2.5.x/PlaySlick)
* [Silhouette 4.0 Authentication](https://www.silhouette.rocks/docs)
* [Scala.js 0.6.22](https://www.scala-js.org/)
* [ScalaRx 0.3.2](https://github.com/lihaoyi/scala.rx)
* [Scalatags 0.4.6](http://www.lihaoyi.com/scalatags/)
* [Autowire 0.2.6](https://github.com/lihaoyi/autowire)
* [CSS Grid](https://scrimba.com/g/gR8PTE)
* Markdown

## Todo

* Add Dartmouth WebAuth support
* Set up the projects to include the shared code correctly.
* Set up sbt to compile both projects automatically
* Add Workbench to reload page whenever there's a change

## Installation

1. Clone the repo.
2. In conf directory, copy `application.conf.seed` to `application.conf` and update if needed.
3. Copy `database.conf.seed` to `database.conf` and fill in your database connection data.
4. Create your database and db user, if needed.
5. Copy `mailer.conf.seed` to `mailer.conf` and optionally fill in your mailer settings (or leave set to mock).
6. Copy `silhouette.conf.seed` to `silhouette.conf` and optionally fill in your authentication settings (Duo values at bottom, get keys from duo.com.  Sign in with your dartmouth.edu email address).
7. [sbt](https://www.scala-sbt.org/download.html) --> project client, fullOptJS, project root, run.

## Dartmouth Web Auth SSO / CAS Authentication

For Ubuntu:

First install these packages needed to build the apache mod: (sudo apt install ...)
* apache2-dev
* libssl-dev
* libcurl4-gnutls-dev
* libpcre3-dev
* automake

Then build the mod.  We need to build this from scratch to get access to the cas attributes (netid!):
```
cd /tmp
git clone https://github.com/Jasig/mod_auth_cas.git
cd mod_auth_cas
git checkout tags/v1.1 (?)
./configure
make
make install

mkdir /var/cache/mod_auth_cas
chown www-data:www-data /var/cache/mod_auth_cas
```

emacs /etc/apache2/mods-available/auth-cas.load
```
LoadModule auth_cas_module /usr/lib/apache2/modules/mod_auth_cas.so
```

emacs /etc/apache2/mods-available/auth-cas.conf
```
CASCookiePath       /var/cache/mod_auth_cas/
CASCertificatePath  /etc/ssl/certs
CASLoginURL         https://login.dartmouth.edu/cas/login
CASValidateURL      https://login.dartmouth.edu/cas/samlValidate
CASValidateSAML     On
CASAttributePrefix  cas-attr-
```

mkdir /var/www/html/cas
echo "Hello There" > /var/www/html/cas/index.html

emacs /etc/apache2/sites-available/[your conf file]
```
<Directory /var/www/html/cas>
  AuthType CAS
  CASAuthNHeader cas
  require valid-user                             # Any CAS user will be able to access the resource
  #require user "Jonathan Crossett@@DARTMOUTH.EDU" # A specific user will have access to the resource
  #require cas-attribute netid:d20964h            # A specific user will have access to the resource identified by NetID
  #require cas-attribute name~^.*Crossett$        # REGEX match - Only people who's name ends with 'Crossett'
  #require cas-attribute affil~^(?!ALUMNI).*$      # REGEX match - Only people that are not ALUMNI
</Directory>
```

a2enmod auth_cas

service httpd restart

browse to:
http://localhost/cas/
