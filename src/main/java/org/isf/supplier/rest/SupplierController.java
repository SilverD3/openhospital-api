/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.supplier.rest;

import java.util.List;

import javax.validation.Valid;

import org.isf.shared.exceptions.OHAPIException;
import org.isf.supplier.dto.SupplierDTO;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.mapper.SupplierMapper;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(value = "/suppliers", produces = MediaType.APPLICATION_JSON_VALUE, authorizations = {@Authorization(value="basicAuth")})
public class SupplierController {

	@Autowired
	private SupplierBrowserManager manager;
	@Autowired
	private SupplierMapper mapper;
	
	/**
	 * Saves the specified {@link SupplierDTO}.
	 * @param suplierDTO
	 * @return <code>true</code> if the supplier was saved
	 * @throws OHServiceException
	 */
	@PostMapping(value = "/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> saveSupplier(@RequestBody @Valid SupplierDTO suplierDTO) throws OHServiceException {
		log.info("Saving a new supplier...");
		boolean isCreated = manager.saveOrUpdate(mapper.map2Model(suplierDTO));
		if (!isCreated) {
			log.error("Supplier is not created!");
            throw new OHAPIException(new OHExceptionMessage(null, "Supplier is not created!", OHSeverityLevel.ERROR));
        }
		log.info("Supplier saved successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(isCreated);
	}
	
	/**
	 * Updates the specified {@link SupplierDTO}.
	 * @param suplierDTO
	 * @return <code>true</code> if the supplier was updated
	 * @throws OHServiceException
	 */
	@PutMapping(value = "/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> updateSupplier(@RequestBody @Valid SupplierDTO suplierDTO) throws OHServiceException {
		if(suplierDTO.getSupId() == null || manager.getByID(suplierDTO.getSupId()) == null) {
			throw new OHAPIException(new OHExceptionMessage(null, "Supplier not found!", OHSeverityLevel.ERROR));
		}
		log.info("Updating supplier...");
		boolean isUpdated = manager.saveOrUpdate(mapper.map2Model(suplierDTO));
		if (!isUpdated) {
			log.error("Supplier is not updated!");
            throw new OHAPIException(new OHExceptionMessage(null, "Supplier is not updated!", OHSeverityLevel.ERROR));
        }
		log.info("Supplier updated successfully");
        return ResponseEntity.ok(isUpdated);
	}
	
	/**
	 * Get the suppliers.
	 * @param excludeDeleted
	 * @return the list of suppliers found
	 * @throws OHServiceException
	 */
	@GetMapping(value = "/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SupplierDTO>> getSuppliers(
			@RequestParam(name="exclude_deleted", defaultValue="true") boolean excludeDeleted) throws OHServiceException {
		log.info("Loading suppliers...");
		List<Supplier> suppliers = excludeDeleted? manager.getList() : manager.getAll();
		List<SupplierDTO> mappedSuppliers = mapper.map2DTOList(suppliers);
		if(mappedSuppliers.isEmpty()) {
			log.info("No supplier found");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(mappedSuppliers);
		} else {
			log.info("Found {} suppliers", mappedSuppliers.size());
			return ResponseEntity.ok(mappedSuppliers);
		}
	}
	
	/**
	 * Get a supplier by its ID.
	 * @param id
	 * @return the found supplier
	 * @throws OHServiceException
	 */
	@GetMapping(value = "/suppliers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SupplierDTO> getSuppliers(@PathVariable Integer id) throws OHServiceException {
		log.info("Loading supplier with ID {}", id);
		Supplier supplier = manager.getByID(id);
		if(supplier == null) {
			log.info("Supplier not found");
			throw new OHAPIException(new OHExceptionMessage(null, "Supplier not found!", OHSeverityLevel.ERROR));
		} else {
			log.info("Found supplier!");
			return ResponseEntity.ok(mapper.map2DTO(supplier));
		}
	}
}