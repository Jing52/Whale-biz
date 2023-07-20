package com.whale.framework.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.whale.framework.common.domain.enums.TemplateDataLogicEnum;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Map的工具类
 * leizhengwei 2018-11-17
 */
public class MapUtil {

    /**
     * 将json字符串转为Map结构
     * 如果json复杂，结果可能是map嵌套map
     * @param jsonStr 入参，json格式字符串
     * @return 返回一个map
     */
    public static Map<String, Object> json2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<>();
        if(jsonStr != null && !"".equals(jsonStr)){
            //最外层解析
            JSONObject json = null;
            json = JSONObject.parseObject(jsonStr);
            for (Object k : json.keySet()) {
                Object v = json.get(k);
                //如果内层还是数组的话，继续解析
                if (v instanceof JSONArray) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    Iterator<Object> it = ((JSONArray) v).iterator();
                    while (it.hasNext()) {
                        JSONObject jsonItem = (JSONObject) it.next();
                        list.add(json2Map(jsonItem.toString()));
                    }
                    map.put(k.toString(), list);
                } else {
                    Object jsonValidObj = isJSONValid(v);
                    if(jsonValidObj instanceof JSONObject){
                        map.put(k.toString(), json2Map(String.valueOf(v)));
                    }else if(jsonValidObj instanceof JSONArray){
                        List<Map<String, Object>> list = new ArrayList<>();
                        Iterator<Object> it = ((JSONArray) jsonValidObj).iterator();
                        while (it.hasNext()) {
                            JSONObject jsonItem = (JSONObject) it.next();
                            list.add(json2Map(jsonItem.toString()));
                        }
                        map.put(k.toString(), list);
                    }else {
                        map.put(k.toString(), v);
                    }
                }
            }
            return map;
        }else{
            return null;
        }
    }

    public final static Object isJSONValid(Object jsonObj) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(String.valueOf(jsonObj));
            return jsonObject;
        } catch (JSONException ex) {
            try {
                JSONArray jsonArray = JSONObject.parseArray(String.valueOf(jsonObj));
                return jsonArray;
            } catch (JSONException ex1) {
                return null;
            }
        }
    }

    
    /**
     * 树节点覆盖 将replaceMap的值覆盖到sourceMap
     * @param sourceMap
     * @param replaceMap
     */
    public static void treeNodeCover(Map<String,Object> sourceMap,Map<String,Object> replaceMap){
        for (String key : replaceMap.keySet()) {
            Object value = sourceMap.get(key);
            if( value != null ){
                if( value instanceof String){
                    sourceMap.put(key,replaceMap.get(key));
                }
                Object replaceValue = replaceMap.get(key);
                if( value instanceof Map  ){
                    treeNodeCover((Map<String,Object>)value,(Map<String,Object>)replaceValue);
                }
            }
        }
    }

    /**
     *  钻井式读取Map数据
     *  针对value是数组进行特殊化处理，增加两种模式
     *    1、wenzhou.ququ=fail 去kv对应数据
     *    2、wenzhou.2   取第几位数据
     * @param key
     * @param map
     * @param compareSign
     * @param size
     * @param keyRight
     * @return
     */
    public static Object getBoreholeMap(String key, Map <String,Object> map, String compareSign, String size, String keyRight){
        String[] keys = key.split("\\.");
        Object value = map;
        for(int i=0;i<keys.length;i++){
            if(value instanceof List){
                if(keys[i].indexOf("=") > -1){
                    String[] kv = keys[i].split("=");
                    for(Map<String,Object> listMap:(List<Map<String,Object>>)value){
                        if(StringUtils.equals((String)listMap.get(kv[0]),kv[1])){
                            value = listMap;
                            break;
                        }
                    }
                }else{
                   if(!ValidationUtil.isInteger(keys[i])){
                       return null;
                   }
                   if(Integer.parseInt(keys[i]) >= ((List) value).size()){
                       return null;
                   }
                   value = ((List) value).get(Integer.parseInt(keys[i]));

                }
            }else {
                value =( (Map<String,Object>) value).get(keys[i]);
            }
            if(i!= (keys.length -1)){
                if(!(value instanceof Map || value instanceof List)){
                    return null;
                }
            }
        }
        if(value instanceof List){
            if(Integer.valueOf(compareSign) == 0 ){//固定值
                if(((List<Map<String,Object>>)value).size() == Integer.valueOf(size)){
                    //对应模版单选框，只有选中一个选项，另外选项则不会选中，所以只对一个赋值就可以。
                    if(keyRight.contains(TemplateDataLogicEnum.RADIO_SYMBOL.getType()))
                        return "1";
                    return boreholeMapGet(key.concat(keyRight), map);
                }
            }else if (Integer.valueOf(compareSign) > 0){//分段
                if(((List<Map<String,Object>>)value).size() > Integer.valueOf(size)){
                    return boreholeMapGet(key.concat(keyRight), map);
                }
            }
        }
        return null;
    }

    public static Object boreholeMapGet(String key,Map <String,Object> map){
        String[] keys = key.split("\\.");
        Object value = map;
        for(int i=0;i<keys.length;i++){
            if(value instanceof List){
                if(keys[i].indexOf("=") > -1){
                    String[] kv = keys[i].split("=");
                    for(Map<String,Object> listMap:(List<Map<String,Object>>)value){
                        if(StringUtils.equals((String)listMap.get(kv[0]),kv[1])){
                            value = listMap;
                            break;
                        }
                    }
                }else{
                    if(!ValidationUtil.isInteger(keys[i])){
                        return null;
                    }
                    if(Integer.parseInt(keys[i]) >= ((List) value).size()){
                        return null;
                    }
                    value = ((List) value).get(Integer.parseInt(keys[i]));

                }
            }else {
                value =( (Map<String,Object>) value).get(keys[i]);
            }
            if(i!= (keys.length -1)){
                if(!(value instanceof Map || value instanceof List)){
                    return null;
                }
            }
        }
        return value;
    }


    public static Object boreholeMapSet(String key,Map <String,Object> map, Object assignValue){
        String[] keys = key.split("\\.");
        Object value = map;
        for(int i=0;i<keys.length;i++){
            if(value instanceof List){
                if(keys[i].indexOf("=") > -1){
                    String[] kv = keys[i].split("=");
                    for(Map<String,Object> listMap:(List<Map<String,Object>>)value){
                        if(StringUtils.equals((String)listMap.get(kv[0]),kv[1])){
                            value = listMap;
                            break;
                        }
                    }
                }else{
                    if(!ValidationUtil.isInteger(keys[i])){
                        return null;
                    }
                    if(Integer.parseInt(keys[i]) >= ((List) value).size()){
                        return null;
                    }
                    //value = ((List) value).get(Integer.parseInt(keys[i]));
                   ((List) value).add(assignValue);

                }
            }else {
               // value =( (Map<String,Object>) value).get(keys[i]);
                ( (Map<String,Object>) value).put(keys[i], assignValue);
            }
            if(i!= (keys.length -1)){
                if(!(value instanceof Map || value instanceof List)){
                    return null;
                }
            }
        }
        return value;
    }

    public static int getDynamicArrNodeSize(String key, Map<String,Object> map){
        Object obj = map.get(key);
        if(obj instanceof List){
            return ((List)obj).size();
        }
        return 0;
    }

    public static Map<String, Object> getDynamicSourceDataMap(String key, Map<String,Object> map, int dynamicIndex){
        Object obj1 = map.get(key);
        if(obj1 instanceof List){
             Object obj2 = ((List)obj1).get(dynamicIndex);
             if(obj2 instanceof Map){
                return (Map)obj2;
             }
        }
        return null;
    }

    public static Object boreholeMapGetList(String key,Map <String,Object> map) {
        StringBuilder result = new StringBuilder();
        String[] keys = key.split("\\.");
        Object value = map;
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            if (value instanceof List) {
                if (keys[i].indexOf("=") > -1) {
                    String[] kv = keys[i].split("=");
                    for (Map<String, Object> listMap : (List<Map<String, Object>>) value) {
                        if (StringUtils.equals((String) listMap.get(kv[0]), kv[1])) {
                            values.add(listMap);
                        }
                    }
                    int count = 1;
                    for (Object obj : values) {
                        String temp = (String) ((Map<String, Object>) obj).get(keys[i+1]);
                        if (count > 1) {
                            result.append(" | ");
                        }
                        result.append(temp);
                        count++;
                    }
                    return result.toString();
                }
            } else {
                value = ((Map<String, Object>) value).get(keys[i]);
            }
        }
        return value;
    }

    public static Map<String, Object> covertObj2Map(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>(16);
        if (Objects.isNull(obj)) {
            return map;
        }
        Class<?> cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String keyName = field.getName();
            Object value = field.get(obj);
            if (value == null) {
                value = "";
            }
            map.put(keyName, value);
        }
        return map;
    }


}
