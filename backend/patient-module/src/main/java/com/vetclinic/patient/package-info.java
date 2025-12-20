/**
 * Patient module - manages animals/pets in the veterinary clinic.
 *
 * <h2>Module Structure (Hexagonal Architecture)</h2>
 * <ul>
 *   <li>{@code api} - Inbound adapters (REST controllers, DTOs)</li>
 *   <li>{@code domain} - Core business logic (entities, services, ports)</li>
 *   <li>{@code infrastructure} - Outbound adapters (repositories, external clients)</li>
 * </ul>
 *
 * <h2>Public API</h2>
 * Other modules should ONLY depend on:
 * <ul>
 *   <li>{@link com.vetclinic.patient.PatientModuleConfig} - to import this module</li>
 *   <li>Classes in {@code domain.model} package - for shared IDs/references</li>
 * </ul>
 */
package com.vetclinic.patient;
