# Hodoor ![Hodoor](https://user-images.githubusercontent.com/38328740/58388164-73505080-7ff1-11e9-8c04-ecdc748141e9.gif)

HTTP Object Door is a library for simplified management of input and output information in JSON, HTTP connections and object instantiations for android.

## Prerequisites

- Android API 15 +

### Installing

Clone this repository in your project and insert into your manifest these permissions:

```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Usage

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