#!/usr/bin/env ruby
# frozen_string_literal: true

require "rexml/document"

ROOT = File.expand_path("..", __dir__)
POM_PATHS = Dir.glob(File.join(ROOT, "**", "pom.xml")).sort
FIXED_VERSION = "yuto-3.0-RELEASE"
DEFAULT_REVISION = "3.0.1-SNAPSHOT"
NEXUS_SNAPSHOTS_URL = "https://10.13.21.7/repository/maven-snapshots/"

def read_xml(path)
  REXML::Document.new(File.read(path))
end

def text_at(document, xpath)
  element = REXML::XPath.first(document, xpath)
  element&.text&.strip
end

def fail_with(errors)
  return if errors.empty?

  warn "Maven versioning verification failed:"
  errors.each { |error| warn "- #{error}" }
  exit 1
end

errors = []
root_pom = File.join(ROOT, "pom.xml")
root_doc = read_xml(root_pom)
maven_config = File.join(ROOT, ".mvn", "maven.config")

errors << "root project version must be ${revision}" unless text_at(root_doc, "/project/version") == "${revision}"
errors << "root revision default must be #{DEFAULT_REVISION}" unless text_at(root_doc, "/project/properties/revision") == DEFAULT_REVISION
errors << ".mvn/maven.config must set -Drevision=#{DEFAULT_REVISION}" unless File.exist?(maven_config) && File.read(maven_config).lines.map(&:strip).include?("-Drevision=#{DEFAULT_REVISION}")
errors << "smart.version must follow ${revision}" unless text_at(root_doc, "/project/properties/smart.version") == "${revision}"
errors << "tool.version must follow ${revision}" unless text_at(root_doc, "/project/properties/tool.version") == "${revision}"
errors << "root smart-common-bom import must follow ${revision}" unless text_at(root_doc, "/project/dependencyManagement/dependencies/dependency[artifactId='smart-common-bom']/version") == "${revision}"
errors << "snapshotRepository id must be maven-snapshots" unless text_at(root_doc, "/project/distributionManagement/snapshotRepository/id") == "maven-snapshots"
errors << "snapshotRepository url must point to maven-snapshots" unless text_at(root_doc, "/project/distributionManagement/snapshotRepository/url") == NEXUS_SNAPSHOTS_URL
errors << "flatten-maven-plugin must be configured in build/plugins" unless text_at(root_doc, "/project/build/plugins/plugin[artifactId='flatten-maven-plugin']/artifactId") == "flatten-maven-plugin"

POM_PATHS.each do |path|
  relative = path.delete_prefix("#{ROOT}/")
  document = read_xml(path)
  xml = File.read(path)

  if text_at(document, "/project/distributionManagement/repository/id") == "maven-releases"
    errors << "#{relative} distributionManagement must include maven-snapshots" unless text_at(document, "/project/distributionManagement/snapshotRepository/id") == "maven-snapshots"
    errors << "#{relative} snapshotRepository url must point to maven-snapshots" unless text_at(document, "/project/distributionManagement/snapshotRepository/url") == NEXUS_SNAPSHOTS_URL
  end

  next unless xml.include?(FIXED_VERSION)

  allowed_root_parent = relative == "pom.xml" &&
                        xml.scan(FIXED_VERSION).size == 1 &&
                        text_at(document, "/project/parent/version") == FIXED_VERSION

  errors << "#{relative} still contains #{FIXED_VERSION}" unless allowed_root_parent
end

fail_with(errors)
puts "Maven versioning verification passed for #{POM_PATHS.size} POM files"
