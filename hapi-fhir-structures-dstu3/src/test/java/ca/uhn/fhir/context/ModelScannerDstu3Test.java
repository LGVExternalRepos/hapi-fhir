package ca.uhn.fhir.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.hl7.fhir.dstu3.model.BaseResource;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Property;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.junit.Test;

import ca.uhn.fhir.model.api.annotation.Compartment;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import ca.uhn.fhir.parser.DataFormatException;

public class ModelScannerDstu3Test {

	@Test
	public void testBundleMustImplementIBaseBundle() throws DataFormatException {
		FhirContext ctx = FhirContext.forDstu3();
		try {
			ctx.getResourceDefinition(MyBundle.class);
			fail();
		} catch (ConfigurationException e) {
			assertEquals("Resource type declares resource name Bundle but does not implement IBaseBundle", e.getMessage());
		}
	}

	/** This failed at one point */
	@Test
	public void testCarePlan() throws DataFormatException {
		FhirContext.forDstu3().getResourceDefinition(CarePlan.class);
	}

	@Test
	public void testExtendedClass() {
		FhirContext ctx = FhirContext.forDstu3();
		ctx.getResourceDefinition(MyPatient.class);

		RuntimeResourceDefinition patient = ctx.getResourceDefinition("Patient");
		assertEquals(Patient.class, patient.getImplementingClass());

		RuntimeResourceDefinition def = ctx.getResourceDefinition(MyPatient.class);
		RuntimeResourceDefinition baseDef = def.getBaseDefinition();
		assertEquals(Patient.class, baseDef.getImplementingClass());
	}

	@Test
	public void testResourceWithNoDef() {
		try {
			FhirContext.forDstu3().getResourceDefinition(NoResourceDef.class);
			fail();
		} catch (ConfigurationException e) {
			assertEquals("Resource class[ca.uhn.fhir.context.ModelScannerDstu3Test$NoResourceDef] does not contain any valid HAPI-FHIR annotations", e.getMessage());
		}
	}

	@Test
	public void testScanExtensionTypes() throws DataFormatException {

		FhirContext ctx = FhirContext.forDstu3();
		RuntimeResourceDefinition def = ctx.getResourceDefinition(ResourceWithExtensionsDstu3A.class);

		assertEquals(RuntimeChildCompositeDatatypeDefinition.class, def.getChildByNameOrThrowDataFormatException("identifier").getClass());

		RuntimeChildDeclaredExtensionDefinition ext = def.getDeclaredExtension("http://foo/#f1");
		assertNotNull(ext);
		BaseRuntimeElementDefinition<?> valueString = ext.getChildByName("valueString");
		assertNotNull(valueString);

		ext = def.getDeclaredExtension("http://foo/#f2");
		assertNotNull(ext);
		valueString = ext.getChildByName("valueString");
		assertNotNull(valueString);

		ext = def.getDeclaredExtension("http://bar/#b1");
		assertNotNull(ext);
		RuntimeChildDeclaredExtensionDefinition childExt = ext.getChildExtensionForUrl("http://bar/#b1/1");
		assertNotNull(childExt);
		BaseRuntimeElementDefinition<?> valueDate = childExt.getChildByName("valueDate");
		assertNotNull(valueDate);
		childExt = ext.getChildExtensionForUrl("http://bar/#b1/2");
		assertNotNull(childExt);
		childExt = childExt.getChildExtensionForUrl("http://bar/#b1/2/1");
		assertNotNull(childExt);
		valueDate = childExt.getChildByName("valueDate");
		assertNotNull(valueDate);

	}

	@Test
	public void testSearchParamWithCompartmentForNonReferenceParam() {
		try {
			FhirContext.forDstu3().getResourceDefinition(CompartmentForNonReferenceParam.class);
			fail();
		} catch (ConfigurationException e) {
			assertEquals("Search param foo provides compartment membershit but is not of type 'reference'", e.getMessage());
		}
	}

	@Test
	public void testSearchParamWithInvalidType() {
		try {
			FhirContext.forDstu3().getResourceDefinition(InvalidParamType.class);
			fail();
		} catch (ConfigurationException e) {
			assertEquals("Search param foo has an invalid type: bar", e.getMessage());
		}
	}

	@ResourceDef(name = "Patient")
	public static class CompartmentForNonReferenceParam extends Patient {
		private static final long serialVersionUID = 1L;

		@SearchParamDefinition(name = "foo", path = "Patient.telecom", type = "string", providesMembershipIn = { @Compartment(name = "Patient"), @Compartment(name = "Device") })
		public static final String SP_TELECOM = "foo";

	}

	@ResourceDef(name = "Patient")
	public static class InvalidParamType extends Patient {
		private static final long serialVersionUID = 1L;

		@SearchParamDefinition(name = "foo", path = "Patient.telecom", type = "bar")
		public static final String SP_TELECOM = "foo";

	}

	@ResourceDef(name = "Bundle")
	public static class MyBundle extends BaseResource {
		private static final long serialVersionUID = 1L;

		@Override
		public String fhirType() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public IIdType getIdElement() {
			return null;
		}

		@Override
		public IPrimitiveType<String> getLanguageElement() {
			return null;
		}

		@Override
		public IBaseMetaType getMeta() {
			return null;
		}

		@Override
		protected void listChildren(List<Property> theResult) {
			// nothing
		}

		@Override
		public IAnyResource setId(String theId) {
			return null;
		}

		@Override
		public BaseResource setIdElement(IdType theIdType) {
			return null;
		}

	}

	class NoResourceDef extends Patient {
		private static final long serialVersionUID = 1L;

		@SearchParamDefinition(name = "foo", path = "Patient.telecom", type = "bar")
		public static final String SP_TELECOM = "foo";

	}

}
