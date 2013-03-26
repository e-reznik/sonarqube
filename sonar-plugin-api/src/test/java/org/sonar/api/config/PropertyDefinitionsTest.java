/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.api.config;

import org.junit.Test;
import org.sonar.api.Properties;
import org.sonar.api.Property;

import static org.fest.assertions.Assertions.assertThat;

public class PropertyDefinitionsTest {

  @Test
  public void should_inspect_plugin_objects() {
    PropertyDefinitions def = new PropertyDefinitions(
        PropertyDefinition.build("foo").name("Foo").build(),
        PropertyDefinition.build("one").name("One").build(),
        PropertyDefinition.build("two").name("Two").defaultValue("2").build()
    );

    assertProperties(def);
  }

  @Test
  public void should_inspect_annotation_plugin_objects() {
    PropertyDefinitions def = new PropertyDefinitions(new PluginWithProperty(), new PluginWithProperties());

    assertProperties(def);
  }

  @Test
  public void should_inspect_plugin_classes() {
    PropertyDefinitions def = new PropertyDefinitions(PluginWithProperty.class, PluginWithProperties.class);

    assertProperties(def);
  }

  @Test
  public void test_categories() {
    PropertyDefinitions def = new PropertyDefinitions(
        PropertyDefinition.build("inCateg").name("In Categ").category("categ").build(),
        PropertyDefinition.build("noCateg").name("No categ").build()
    );

    assertThat(def.getCategory("inCateg")).isEqualTo("categ");
    assertThat(def.getCategory("noCateg")).isEmpty();
  }

  @Test
  public void test_categories_on_annotation_plugin() {
    PropertyDefinitions def = new PropertyDefinitions(Categories.class);

    assertThat(def.getCategory("inCateg")).isEqualTo("categ");
    assertThat(def.getCategory("noCateg")).isEqualTo("");
  }

  @Test
  public void test_default_category() {
    PropertyDefinitions def = new PropertyDefinitions();
    def.addComponent(PropertyDefinition.build("inCateg").name("In Categ").category("categ").build(), "default");
    def.addComponent(PropertyDefinition.build("noCateg").name("No categ").build(), "default");

    assertThat(def.getCategory("inCateg")).isEqualTo("categ");
    assertThat(def.getCategory("noCateg")).isEqualTo("default");
  }

  @Test
  public void test_default_category_on_annotation_plugin() {
    PropertyDefinitions def = new PropertyDefinitions();
    def.addComponent(Categories.class, "default");
    assertThat(def.getCategory("inCateg")).isEqualTo("categ");
    assertThat(def.getCategory("noCateg")).isEqualTo("default");
  }

  @Test
  public void should_group_by_category() {
    PropertyDefinitions def = new PropertyDefinitions(
        PropertyDefinition.build("global1").name("Global1").category("catGlobal1").global(true).project(false).module(false).build(),
        PropertyDefinition.build("global2").name("Global2").category("catGlobal1").global(true).project(false).module(false).build(),
        PropertyDefinition.build("global3").name("Global3").category("catGlobal2").global(true).project(false).module(false).build(),
        PropertyDefinition.build("project").name("Project").category("catProject").global(false).project(true).module(false).build(),
        PropertyDefinition.build("module").name("Module").category("catModule").global(false).project(false).module(true).build()
    );

    assertThat(def.getGlobalPropertiesByCategory().keySet()).containsOnly("catGlobal1", "catGlobal2");
    assertThat(def.getProjectPropertiesByCategory().keySet()).containsOnly("catProject");
    assertThat(def.getModulePropertiesByCategory().keySet()).containsOnly("catModule");
  }

  @Test
  public void should_group_by_category_on_annotation_plugin() {
    PropertyDefinitions def = new PropertyDefinitions(ByCategory.class);

    assertThat(def.getGlobalPropertiesByCategory().keySet()).containsOnly("catGlobal1", "catGlobal2");
    assertThat(def.getProjectPropertiesByCategory().keySet()).containsOnly("catProject");
    assertThat(def.getModulePropertiesByCategory().keySet()).containsOnly("catModule");
  }

  private void assertProperties(PropertyDefinitions definitions) {
    assertThat(definitions.get("foo").name()).isEqualTo("Foo");
    assertThat(definitions.get("one").name()).isEqualTo("One");
    assertThat(definitions.get("two").name()).isEqualTo("Two");
    assertThat(definitions.get("unknown")).isNull();

    assertThat(definitions.getDefaultValue("foo")).isNull();
    assertThat(definitions.getDefaultValue("two")).isEqualTo("2");

    assertThat(definitions.getAll().size()).isEqualTo(3);
  }

  @Property(key = "foo", name = "Foo")
  static final class PluginWithProperty {
  }

  @Properties({
      @Property(key = "one", name = "One"),
      @Property(key = "two", name = "Two", defaultValue = "2")
  })
  static final class PluginWithProperties {
  }

  @Properties({
      @Property(key = "inCateg", name = "In Categ", category = "categ"),
      @Property(key = "noCateg", name = "No categ")
  })
  static final class Categories {
  }

  @Properties({
      @Property(key = "global1", name = "Global1", category = "catGlobal1", global = true, project = false, module = false),
      @Property(key = "global2", name = "Global2", category = "catGlobal1", global = true, project = false, module = false),
      @Property(key = "global3", name = "Global3", category = "catGlobal2", global = true, project = false, module = false),
      @Property(key = "project", name = "Project", category = "catProject", global = false, project = true, module = false),
      @Property(key = "module", name = "Module", category = "catModule", global = false, project = false, module = true)
  })
  static final class ByCategory {
  }
}
