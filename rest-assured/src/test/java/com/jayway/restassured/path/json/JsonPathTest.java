/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.restassured.path.json;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.path.json.JsonPath.from;
import static com.jayway.restassured.path.json.JsonPath.given;
import static com.jayway.restassured.path.json.JsonPath.with;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class JsonPathTest {

    private final String JSON = "{ \"store\": {\n" +
            "    \"book\": [ \n" +
            "      { \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"price\": 8.95\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Evelyn Waugh\",\n" +
            "        \"title\": \"Sword of Honour\",\n" +
            "        \"price\": 12\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Herman Melville\",\n" +
            "        \"title\": \"Moby Dick\",\n" +
            "        \"isbn\": \"0-553-21311-3\",\n" +
            "        \"price\": 8.99\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"J. R. R. Tolkien\",\n" +
            "        \"title\": \"The Lord of the Rings\",\n" +
            "        \"isbn\": \"0-395-19395-8\",\n" +
            "        \"price\": 22.99\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bicycle\": {\n" +
            "      \"color\": \"red\",\n" +
            "      \"price\": 19.95,\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private final String JSON2 = "[{\"email\":\"name1@mail.com\",\"alias\":\"name one\",\"phone\":\"3456789\"},\n" +
            "{\"email\":\"name2@mail.com\",\"alias\":\"name two\",\"phone\":\"1234567\"},\n" +
            "{\"email\":\"name3@mail.com\",\"alias\":\"name three\",\"phone\":\"2345678\"}]";

    @Test
    public void getList() throws Exception {
        final List<String> categories = new JsonPath(JSON).get("store.book.category");
        assertThat(categories.size(), equalTo(4));
        assertThat(categories, hasItems("reference", "fiction"));
    }

    @Test
    public void firstBookCategory() throws Exception {
        final String category = with(JSON).get("store.book[0].category");
        assertThat(category, equalTo("reference"));
    }

    @Test
    public void lastBookTitle() throws Exception {
        final String title = with(JSON).get("store.book[-1].title");
        assertThat(title, equalTo("The Lord of the Rings"));
    }

    @Test
    public void booksBetween5And15() throws Exception {
        final List<Map<String, ?>> books = with(JSON).get("store.book.findAll { book -> book.price >= 5 && book.price <= 15 }");
        assertThat(books.size(), equalTo(3));

        final String author = (String) books.get(0).get("author");
        assertThat(author, equalTo("Nigel Rees"));

        final int price = (Integer) books.get(1).get("price");
        assertThat(price, equalTo(12));
    }

    @Test
    public void sizeInPath() throws Exception {
        final Integer size = with(JSON).get("store.book.size()");
        assertThat(size, equalTo(4));
    }

    @Test
    public void getRootObjectAsMap() throws Exception {
        final Map<String, Map> store = given(JSON).get("store");
        assertThat(store.size(), equalTo(2));

        final Map<String, Object> bicycle = store.get("bicycle");
        final String color = (String) bicycle.get("color");
        final double price = (Double) bicycle.get("price");
        assertThat(color, equalTo("red"));
        assertThat(price, equalTo(19.95d));
    }

    @Test
    public void rootPath() throws Exception {
        final JsonPath jsonPath = new JsonPath(JSON).setRoot("store.book");
        assertThat(jsonPath.getInt("size()"), equalTo(4));
        assertThat(jsonPath.getList("author", String.class), hasItem("J. R. R. Tolkien"));

    }

    @Test
    public void supportsGettingEntireObjectGraphUsingEmptyString() throws Exception {
        final List<Map<String, String>> object = from(JSON2).get("");
        assertThat(object.get(0).get("email"), equalTo("name1@mail.com"));
    }

    @Test
    public void supportsGettingEntireObjectGraphUsing$() throws Exception {
        final List<Map<String, String>> object = from(JSON2).get("$");
        assertThat(object.get(0).get("email"), equalTo("name1@mail.com"));
    }

    @Test
    public void getValueFromUnnamedRootObject() throws Exception {
        final Map<String, String> object = from(JSON2).get("get(0)");
        assertThat(object.get("email"), equalTo("name1@mail.com"));
    }
}
