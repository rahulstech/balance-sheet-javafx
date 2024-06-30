package rahulstech.jfx.balancesheet.database.entity;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@DatabaseTable(tableName = "categories")
public class Category {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private long id;

    @DatabaseField(canBeNull = false, unique = true)
    private String name;

    // Constructors
    public Category() {
        // ORMLite needs a no-arg constructor
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
