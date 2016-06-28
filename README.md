# xjc-classref-mapper

A simple stab at a plugin for handling classref when using wsimport.

## Usecase

Multiple WSDL (SOAP) services share a common header part (session).
and you want to encapsulate the services because they evolve indenpendently (hence the multiple wsdl-files)
But the authentication (session) is shared.

You want to mimic this at the consumer end.

XJC supports "class ref" for binding Elements to existing classes, but this in not exposed in the Outline,  
which is used to create a JAXBModel which in turns is used by WSImport.

The plugin will find all jaxb bindings with <class ref=""/> create an elementmapping(CElementInfo).  
CElementInfo won't create a class, but an ObjectFactory will be created.  
So the plugin will later remove that ObjectFactory so that it wont shadow the shared lib ObjectFactory.   
(more precisely it will remove the class ref="" package and all it's classes)

add jar to wsImport classpath add xjc argument -classref and it "should" produce generated code with references to existing.
 
## When to try this plugin

You get the following typ of message when using wsimport:  
```
 [ERROR] Schema descriptor {[namespace]}[element] in message part "[part]" is not defined and could not be bound to Java. Perhaps the schema descriptor {[namespace]}[element] is not defined in the schema imported/included in the WSDL. You can either add such imports/includes or run wsimport and provide the schema location using -b switch.
```

# Disclaimer this has not been tested at runtime yet!!! It generates, it compiles, but it has not yet been used.

## Usage

We/I are on bintray
Adde repo and dependency


```
repository {
    maven {
        url  "http://dl.bintray.com/npetzall/maven"
    }
}
dependencies {
    compile 'npetzall.xjc.plugin:xjc-classref-mapper:0.0.1'
}
```

