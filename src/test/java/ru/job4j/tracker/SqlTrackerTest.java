package ru.job4j.tracker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

class SqlTrackerTest {
    private static Connection connection;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("db/liquibase_test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertThat(tracker.findById(item.getId())).isEqualTo(item);
    }
    @Test
    public void whenDeleteItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        tracker.delete(item.getId());
        assertThat(tracker.findById(item.getId())).isNull();
    }

    @Test
    public void whenReplaceItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        Item item1 = new Item("item1");
        tracker.replace(item.getId(), item1);
        assertThat(tracker.findById(item.getId()).equals(item1));
    }

    @Test
    public void whenFindAllItems() {
        SqlTracker tracker = new SqlTracker(connection);
        Item first = new Item("first");
        Item second = new Item("second");
        Item third = new Item("Third");

        List<Item> items = new ArrayList<>();
        items.add(tracker.add(first));
        items.add(tracker.add(second));
        items.add(tracker.add(third));
        assertThat(tracker.findAll()).isNotEmpty();
        assertThat(items).isEqualTo(tracker.findAll());
    }

    @Test
    public void whenNoItemNameThanEmptyList() {
        SqlTracker tracker = new SqlTracker(connection);
        Item first = new Item("first");
        Item second = new Item("second");
        Item third = new Item("Third");

        tracker.add(first);
        tracker.add(second);
        tracker.add(third);
        assertThat(tracker.findByName("four")).isEmpty();
    }
    @Test
    public void whenFindByIdNoItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertThat(tracker.findById(item.getId() + 1)).isNull();
    }

    @Test
    public void whenFindByName() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        List<Item> items = new ArrayList<>();
        items.add(tracker.add(item));
        assertThat(tracker.findByName("item")).isEqualTo(items);
    }
}