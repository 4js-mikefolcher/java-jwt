# java-jwt
Java implementation of JSON Web Token (JWT) 

## Getting Started
**Prerequisite**\
Before you get started, the org.json jar file must be downloaded and referenced in CLASSPATH.\
You can download the latest version here: https://jar-download.com/artifacts/org.json

**Download & Build**
- Download this Git repository to your local machine
- Run the jar-build.sh command to build the jar file
- Reference the jwt-fourjs.jar file in your CLASSPATH

## Usage


### Generate JWT Token 

The following code can be used to generate JWT token in Java

**Java Implementation**
```java

import com.fourjs.jwt.JWebToken;

String subject = "John";
String[] audience = new String[]{"Admin", "Accounting", "Payroll"};
int expireMinutes = 30;
String secretKey = "ThisIsMySpecialSecretKey:OU812!";

JWebToken token = JWebToken.CreateToken(subject, audience, expireMinutes, secretKey);

String bearerToken = token.toString();

```

The following code can be used to generate a JWT token in Genero

**Genero Implementation**
```genero
IMPORT JAVA com.fourjs.jwt.JWebToken
IMPORT JAVA java.lang.String
IMPORT JAVA java.lang.Object

PUBLIC TYPE JavaString java.lang.String
PUBLIC TYPE JavaStringArray ARRAY[] OF JavaString
PUBLIC TYPE JavaObjectArray ARRAY[] OF java.lang.Object

PUBLIC TYPE TWebToken com.fourjs.jwt.JWebToken

FUNCTION buildToken() RETURNS ()
   DEFINE token TWebToken
   DEFINE permissions DYNAMIC ARRAY OF STRING = ["Admin", "Accounting", "Payroll"];
   DEFINE javaPermissions JavaStringArray
   DEFINE idx INTEGER
   DEFINE expireMinutes INTEGER
   DEFINE secretKey STRING
   DEFINE subject STRING
   DEFINE bearerToken STRING

   LET subject = "John" 
   LET expireMinutes = 30
   LET secretKey = "ThisIsMySpecialSecretKey:OU812!"
   LET javaPermissions = JavaStringArray.create(permissions.getLength())
   FOR idx = 1 TO permissions.getLength()
      LET javaPermissions[idx] = permissions[idx]
   END FOR

   LET token = TWebToken.CreateToken(subject, javaPermissions, expireMinutes, secretKey)

   LET bearerToken = token.toString()

END FUNCTION

```

### Verify JWT Token 

JWT token recieved in the String format can be used to verify and extract audience and subject information as follows.

**Java Implementation**
```java
//verify and use
String secretKey = "ThisIsMySpecialSecretKey:OU812!";
JWebToken incomingToken = new JWebToken(bearerToken, secretKey);
if (incomingToken.isValid()) {
    List<String> audience = incomingToken.getAudience();
    String subject = incomingToken.getSubject();
}
```

**Genero Implementation**
```genero
IMPORT JAVA com.fourjs.jwt.JWebToken
IMPORT JAVA java.lang.String
IMPORT JAVA java.lang.Object

PUBLIC TYPE JavaString java.lang.String
PUBLIC TYPE JavaStringArray ARRAY[] OF JavaString
PUBLIC TYPE JavaObjectArray ARRAY[] OF java.lang.Object

PUBLIC TYPE TWebToken com.fourjs.jwt.JWebToken

FUNCTION verifyToken(bearerToken STRING) RETURNS (BOOLEAN)
   DEFINE token TWebToken
   DEFINE secretKey STRING

   LET secretKey = "ThisIsMySpecialSecretKey:OU812!"
   LET token = TWebToken.create(bearerToken, secretKey)
   
   RETURN token.isValid()

END FUNCTION
```

### JWT Tutorial 
This code was modified from the original implementation done by com.metamug\
Here is the original tutorial.
https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
