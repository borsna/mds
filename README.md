# Overview

This is Metadata Store (MDS) for DataCite central infrastructure. This
app has UI and API for DataCite members and their datacentres. The
main functionality is minting DOIs and storing metadata for the
datasets.

To learn more about DataCite please visit [our website](http://www.datacite.org)

To use this software please go to [https://mds.datacite.org](https://mds.datacite.org)

# Installation (for development only)

## Tools

It's assumed you have PosgreSQL 8.4 and Roo 1.1 installed. You also
will need Maven 2.2.1 and JDK 6 in your system (OpenJDK from Ubuntu
works fine).

You don't need Roo to run the code. Although you will most likely need
it for development.

### mysql setup

Create database with UTF-8 support

    create database datacite character set utf8;
    create database datacite_test character set utf8;

make sure transactions are supported - add in my.cnf:

    default-storage-engine=innodb

## Java dependencies

Most dependencies are managed by Maven public repositories. There is
one jar you need to download and add to your local maven repo
manually.

#### Handle API client

MDS uses Handle System Java API to make calls to the Handle Service. You
need the Handle API client jar.

Download the Java package from [Handle.net](http://handle.net/client_download.html)

Extract files and add handle-client.jar to your local maven repo:

    mvn install:install-file -Dfile=handle-client.jar -DgroupId=handle.net \
     -Dversion=7 -Dpackaging=jar -DgeneratePom=true -DartifactId=hcj

### Local SSL cert

By default MDS uses https for all URLs. In order to use SSL locally
you need a SSL certificate. This certificate can be self generated -
the browser will complain but all will work OK.

    keytool -genkey -alias tomcat -keyalg RSA -dname "cn=localhost"

The default password is 'changeit'

## Configure the source code 

I assume you had created a fork from the master DataCite
repository. Now you need to configure the code before compiling. 

The git repository has a bunch of *.template files. You can find them
with:

    find . -name *.template

Those files are templates for the various configuration files which
are machine specific i.e. passwords, IP addresses etc.

To customise them you need to make a copy omitting (.template from
file name) e.g.:

    cp src/main/resources/META-INF/persistence.xml.template \
     src/main/resources/META-INF/persistence.xml

Now in such created file you need to adjust values according to your
local environment.

### src/main/resources/META-INF/persistence.xml

    <property name="hibernate.hbm2ddl.auto" value="create"/>

set the value to 'create' for the first run (DDL script will be run),
for consecutive runs use 'validate'.

### src/main/resources/META-INF/spring/salt.properties

values put there will be used for password hashing.

### src/main/resources/META-INF/spring/database.properties

your database configuration, password etc as you typed then creating
the users.

### src/test/resources/META-INF/spring/database.properties

your test database configuration. This database is recreated from
scratch every time test run via "mvn clean test"

### src/main/resources/META-INF/spring/handle.properties

Handle service authentication info. If you don't know what to put here
check DataCite wiki (only for members) or setup your own Handle
Service.

### src/main/resources/META-INF/spring/email.properties

your SMTP settings. Use your own SMPT or check DataCite wiki.

### src/main/resources/META-INF/spring/xml-validator.properties

location of XSD and flag if XML should be validated.

### src/main/resources/log4j.properties

your usual log4j stuff.

## Running locally 

### First run

At this stage you should be able to run application.

    mvn compile tomcat:run

point your browser at https://localhost:8443/mds/

it will complain about untrusted SSL certificate but you say it's OK
and the main page of MDS should be presented.

Now kill tomcat and change src/main/resources/META-INF/persistence.xml

### Creating user accounts

To login and create accounts for the users you need to insert admin
account. Therefore run

    mvn exec:java -Dexec.mainClass=org.datacite.mds.tools.AdminAccountCreator
    
You will be asked to specify symbol, password and e-mail for the admin account.

### That's all!

You can run: 

    mvn compile tomcat:run 

again and create appropriate accounts and prefixes.


