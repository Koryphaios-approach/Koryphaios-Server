package fr.obeo.koryphaios.common.dto;

public sealed interface IInput permits IContributedEventInput, NewContributionInput, NewIntegrationInput {

    String studyId();

}
