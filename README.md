# Hodoor ![Hodoor](https://user-images.githubusercontent.com/38328740/58388164-73505080-7ff1-11e9-8c04-ecdc748141e9.gif)

HTTP Object Door is a library for simplified management of input and output information in JSON, HTTP connections and object instantiations for android.

## Prerequisites

- Android API 15 +

## Installing

Clone this repository in your project and insert into your manifest these permissions:

```XML
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Usage

To use Hodoor in an Activity, you must implement its interface:

```java
public class MainActivity extends AppCompatActivity  implements Hodoor.Response {
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ....

```

And then the methods:
```java
    @Override
    public void HttpObjectResponse(Object o, Integer id) {
        
    }

    @Override
    public void HttpListResponse(List<?> l, Integer id) {
        
    }

    @Override
    public void HttpResponseError(Integer hodoorError, Integer networkResponseError, Integer id) {
        
    }
```

#### OK... But... Why do I need this lib?

This library will help you write fewer codes. You will make as many HTTP connections as necessary and will manage the objects sent and received on those connections asynchronously.

Imagine that you have the following class Person:

```java
public class Person {
    
    private String name;
    private int age;

    private Person(){} // <-- This constructor will be used by Hodoor

    @Override
    public String toString() {
        return "name: "+name+" | age: "+age;
    }
}
```

and after the connection you will receive the following JSON response from the server:

```json
{"name":"Jon Snow", "age":30}
```

Now, see how simple it is to retrieve this value and transform it into an object:

```java
public class MainActivity extends AppCompatActivity  implements Hodoor.Response {

    private Hodoor<Person> hodoor; // <-- Hodoor Object with type Person

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize
        hodoor = new Hodoor<>(getApplicationContext(), "http://www.myapi.com", Person.class);
        //                     # Context                # url                  # Person class

        hodoor.setResponse(this); // configure where the response will be sent
        hodoor.send(); // do it

    }

    @Override
    public void HttpObjectResponse(Object o, Integer id) {
        Peson person = (Person)o; // this is your object
    }

```