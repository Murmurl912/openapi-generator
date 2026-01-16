/*
 * Copyright 2021 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.dart.dio;

import org.openapitools.codegen.*;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.openapitools.codegen.languages.DartDioClientCodegen;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DartDioClientCodegenTest {

    @Test
    public void testInitialConfigValues() throws Exception {
        final DartDioClientCodegen codegen = new DartDioClientCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
        Assert.assertTrue(codegen.isHideGenerationTimestamp());
    }

    @Test
    public void testInitialFeatures() {
        final DartDioClientCodegen codegen = new DartDioClientCodegen();
        codegen.processOpts();

        Assert.assertNotNull(codegen.getFeatureSet().getSecurityFeatures());
        Assert.assertFalse(codegen.getFeatureSet().getSecurityFeatures().isEmpty());
    }

    @Test
    public void testSettersForConfigValues() throws Exception {
        final DartDioClientCodegen codegen = new DartDioClientCodegen();
        codegen.setHideGenerationTimestamp(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
        Assert.assertFalse(codegen.isHideGenerationTimestamp());
    }

    @Test
    public void testAdditionalPropertiesPutForConfigValues() throws Exception {
        final DartDioClientCodegen codegen = new DartDioClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
        Assert.assertFalse(codegen.isHideGenerationTimestamp());
    }

    @Test
    public void testKeywords() {
        final DartDioClientCodegen codegen = new DartDioClientCodegen();

        List<String> reservedWordsList = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/dart/dart-keywords.txt"), StandardCharsets.UTF_8));
            while (reader.ready()) {
                reservedWordsList.add(reader.readLine());
            }
            reader.close();
        } catch (Exception e) {
            String errorString = String.format(Locale.ROOT, "Error reading dart keywords: %s", e);
            Assert.fail(errorString, e);
        }

        Assert.assertTrue(reservedWordsList.size() > 20);
        Assert.assertEquals(codegen.reservedWords().size(), reservedWordsList.size());
        for (String keyword : reservedWordsList) {
            // reserved words are stored in lowercase
            Assert.assertTrue(codegen.reservedWords().contains(keyword.toLowerCase(Locale.ROOT)), String.format(Locale.ROOT, "%s, part of %s, was not found in %s", keyword, reservedWordsList, codegen.reservedWords().toString()));
        }
    }

    @Test
    public void verifyDartDioGeneratorRuns() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("dart-dio")
                .setGitUserId("my-user")
                .setGitRepoId("my-repo")
                .setPackageName("my-package")
                .setInputSpec("src/test/resources/3_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        ClientOptInput opts = configurator.toClientOptInput();

        Generator generator = new DefaultGenerator().opts(opts);
        List<File> files = generator.generate();
        files.forEach(File::deleteOnExit);

        TestUtils.ensureContainsFile(files, output, "README.md");
        TestUtils.ensureContainsFile(files, output, "lib/src/api.dart");
    }

    @Test
    public void verifyDartDioGeneratorRunsWithFreezed() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("dart-dio")
                .setGitUserId("my-user")
                .setGitRepoId("my-repo")
                .setPackageName("my-package")
                .setInputSpec("src/test/resources/3_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"))
                .addAdditionalProperty(CodegenConstants.SERIALIZATION_LIBRARY, DartDioClientCodegen.SERIALIZATION_LIBRARY_FREEZED);

        ClientOptInput opts = configurator.toClientOptInput();

        Generator generator = new DefaultGenerator().opts(opts);
        List<File> files = generator.generate();
        files.forEach(File::deleteOnExit);

        TestUtils.ensureContainsFile(files, output, "README.md");
        TestUtils.ensureContainsFile(files, output, "lib/src/api.dart");
        TestUtils.ensureContainsFile(files, output, "lib/src/model/pet.dart");
        TestUtils.ensureContainsFile(files, output, "build.yaml");

        String petModel = Files.readString(new File(output, "lib/src/model/pet.dart").toPath());
        Assert.assertTrue(petModel.contains("@freezed"));
        Assert.assertTrue(petModel.contains("part 'pet.freezed.dart';"));
        Assert.assertTrue(petModel.contains("part 'pet.g.dart';"));

        String pubspec = Files.readString(new File(output, "pubspec.yaml").toPath());
        Assert.assertTrue(pubspec.contains("freezed_annotation"));
        Assert.assertFalse(pubspec.contains("copy_with_extension"));
        Assert.assertFalse(pubspec.contains("equatable"));
    }

    @Test
    public void verifyDartDioGeneratorRunsWithResultDart() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("dart-dio")
                .setGitUserId("my-user")
                .setGitRepoId("my-repo")
                .setPackageName("my-package")
                .setInputSpec("src/test/resources/3_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"))
                .addAdditionalProperty(DartDioClientCodegen.USE_RESULT_DART, true);

        ClientOptInput opts = configurator.toClientOptInput();

        Generator generator = new DefaultGenerator().opts(opts);
        List<File> files = generator.generate();
        files.forEach(File::deleteOnExit);

        TestUtils.ensureContainsFile(files, output, "lib/src/api/pet_api.dart");

        String petApi = Files.readString(new File(output, "lib/src/api/pet_api.dart").toPath());
        Assert.assertTrue(petApi.contains("Result<Response<"));
        Assert.assertTrue(petApi.contains("package:result_dart/result_dart.dart"));

        String pubspec = Files.readString(new File(output, "pubspec.yaml").toPath());
        Assert.assertTrue(pubspec.contains("result_dart"));
    }

    @Test
    public void verifyDartDioGeneratorRunsWithFreezedDiscriminatorUnion() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("dart-dio")
                .setGitUserId("my-user")
                .setGitRepoId("my-repo")
                .setPackageName("my-package")
                .setInputSpec("src/test/resources/3_0/oneof_polymorphism_and_inheritance.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"))
                .addAdditionalProperty(CodegenConstants.SERIALIZATION_LIBRARY, DartDioClientCodegen.SERIALIZATION_LIBRARY_FREEZED);

        ClientOptInput opts = configurator.toClientOptInput();

        Generator generator = new DefaultGenerator().opts(opts);
        List<File> files = generator.generate();
        files.forEach(File::deleteOnExit);

        TestUtils.ensureContainsFile(files, output, "lib/src/model/foo_ref_or_value.dart");

        String model = Files.readString(new File(output, "lib/src/model/foo_ref_or_value.dart").toPath());
        Assert.assertTrue(model.contains("@Freezed(unionKey: r'@type')"));
        Assert.assertTrue(model.contains("@FreezedUnionValue(r'Foo')"));
        Assert.assertTrue(model.contains("const factory FooRefOrValue.foo"));
        Assert.assertTrue(model.contains("part 'foo_ref_or_value.g.dart';"));
    }

    @Test
    public void verifyDartDioGeneratorInlineDeserializeForContainerAndEnum() throws IOException {
        String api = generateInlineDeserializeApi(false);

        Assert.assertTrue(api.contains("map((dynamic value)"));
        Assert.assertTrue(api.contains("Pet.fromJson("));
        Assert.assertTrue(api.contains("MapEntry"));
        Assert.assertTrue(api.contains("Status.fromJson"));
    }

    @Test
    public void verifyDartDioGeneratorInlineDeserializeForContainerAndEnumWithFreezed() throws IOException {
        String api = generateInlineDeserializeApi(true);

        Assert.assertTrue(api.contains("map((dynamic value)"));
        Assert.assertTrue(api.contains("Pet.fromJson("));
        Assert.assertTrue(api.contains("MapEntry"));
        Assert.assertTrue(api.contains("Status.fromJson"));
    }

    private String generateInlineDeserializeApi(boolean useFreezed) throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        File specFile = File.createTempFile("dart-dio-inline", ".yaml");
        specFile.deleteOnExit();
        String spec = String.join("\n",
                "openapi: 3.0.3",
                "info:",
                "  title: Dart Dio Inline Deserialize",
                "  version: 1.0.0",
                "paths:",
                "  /pets:",
                "    get:",
                "      tags:",
                "        - Inline",
                "      operationId: getPets",
                "      responses:",
                "        '200':",
                "          description: ok",
                "          content:",
                "            application/json:",
                "              schema:",
                "                type: array",
                "                items:",
                "                  $ref: '#/components/schemas/Pet'",
                "  /statuses:",
                "    get:",
                "      tags:",
                "        - Inline",
                "      operationId: getStatuses",
                "      responses:",
                "        '200':",
                "          description: ok",
                "          content:",
                "            application/json:",
                "              schema:",
                "                type: array",
                "                items:",
                "                  $ref: '#/components/schemas/Status'",
                "  /status-map:",
                "    get:",
                "      tags:",
                "        - Inline",
                "      operationId: getStatusMap",
                "      responses:",
                "        '200':",
                "          description: ok",
                "          content:",
                "            application/json:",
                "              schema:",
                "                type: object",
                "                additionalProperties:",
                "                  $ref: '#/components/schemas/Status'",
                "components:",
                "  schemas:",
                "    Pet:",
                "      type: object",
                "      properties:",
                "        id:",
                "          type: integer",
                "          format: int64",
                "        name:",
                "          type: string",
                "    Status:",
                "      type: string",
                "      enum:",
                "        - available",
                "        - pending",
                "        - sold",
                ""
        );
        Files.write(specFile.toPath(), spec.getBytes(StandardCharsets.UTF_8));

        CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("dart-dio")
                .setGitUserId("my-user")
                .setGitRepoId("my-repo")
                .setPackageName("my-package")
                .setInputSpec(specFile.getAbsolutePath().replace("\\", "/"))
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        if (useFreezed) {
            configurator.addAdditionalProperty(CodegenConstants.SERIALIZATION_LIBRARY, DartDioClientCodegen.SERIALIZATION_LIBRARY_FREEZED);
        }

        ClientOptInput opts = configurator.toClientOptInput();

        Generator generator = new DefaultGenerator().opts(opts);
        List<File> files = generator.generate();
        files.forEach(File::deleteOnExit);

        TestUtils.ensureContainsFile(files, output, "lib/src/api/inline_api.dart");

        return Files.readString(new File(output, "lib/src/api/inline_api.dart").toPath());
    }
}
