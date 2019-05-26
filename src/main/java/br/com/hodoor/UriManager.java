package br.com.hodoor;


public class UriManager {

    private String base;
    private String value;

    public UriManager(String base){
        this.base = base;
    }

    public UriManager(String base, String... values){
        this.base = base;
        setValue(values);
    }

    public void setValue(String... values){
        int i = 1;
        String result = base;
        for (String s: values) {
            result = result.replaceFirst("\\$"+i,s);
            i++;
        }
        value = result;
    }

    @Override
    public String toString(){
        return value;
    }

}
