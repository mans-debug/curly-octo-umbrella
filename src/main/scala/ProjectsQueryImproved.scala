package ru.starfish

import org.gitlab.api.query.ProjectsQuery

class ProjectsQueryImproved extends ProjectsQuery{
  def isSearchNamespace(searchNamespaces: Boolean): ProjectsQueryImproved = {
    append("search_namespaces", searchNamespaces.toString)
    this
  }
}
