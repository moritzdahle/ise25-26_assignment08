package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
import de.seuhd.campuscoffee.domain.model.enums.CampusType;
import de.seuhd.campuscoffee.domain.model.enums.PosType;
import de.seuhd.campuscoffee.domain.model.objects.Pos;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CrudServiceImpl.
 * Tests the abstract CRUD service implementation using a concrete test implementation with Pos.
 */
@ExtendWith(MockitoExtension.class)
class CrudServiceTest {

    @Mock
    private PosDataService mockDataService;

    private TestPosService crudService;

    @BeforeEach
    void setUp() {
        crudService = new TestPosService(mockDataService);
    }

    @Test
    void clearShouldCallDataServiceClear() {
        // when
        crudService.clear();

        // then
        verify(mockDataService).clear();
    }

    @Test
    void getAllShouldReturnAllEntities() {
        // given
        Pos entity1 = createTestPos(1L, "POS 1");
        Pos entity2 = createTestPos(2L, "POS 2");
        List<Pos> expectedEntities = Arrays.asList(entity1, entity2);
        when(mockDataService.getAll()).thenReturn(expectedEntities);

        // when
        List<Pos> result = crudService.getAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(entity1, entity2);
        verify(mockDataService).getAll();
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoEntities() {
        // given
        when(mockDataService.getAll()).thenReturn(List.of());

        // when
        List<Pos> result = crudService.getAll();

        // then
        assertThat(result).isEmpty();
        verify(mockDataService).getAll();
    }

    @Test
    void getByIdShouldReturnEntity() {
        // given
        Long id = 1L;
        Pos expectedEntity = createTestPos(id, "Test POS");
        when(mockDataService.getById(id)).thenReturn(expectedEntity);

        // when
        Pos result = crudService.getById(id);

        // then
        assertThat(result).isEqualTo(expectedEntity);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Test POS");
        verify(mockDataService).getById(id);
    }

    @Test
    void getByIdShouldThrowNotFoundExceptionWhenEntityDoesNotExist() {
        // given
        Long id = 999L;
        when(mockDataService.getById(id)).thenThrow(new NotFoundException(Pos.class, id));

        // when & then
        assertThrows(NotFoundException.class, () -> crudService.getById(id));
        verify(mockDataService).getById(id);
    }

    @Test
    void upsertShouldCreateNewEntityWhenIdIsNull() {
        // given
        Pos newEntity = createTestPos(null, "New POS");
        Pos savedEntity = createTestPos(1L, "New POS");
        when(mockDataService.upsert(newEntity)).thenReturn(savedEntity);

        // when
        Pos result = crudService.upsert(newEntity);

        // then
        assertThat(result).isEqualTo(savedEntity);
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("New POS");
        verify(mockDataService).upsert(newEntity);
        verify(mockDataService, never()).getById(any());
    }

    @Test
    void upsertShouldUpdateExistingEntityWhenIdIsPresent() {
        // given
        Long id = 1L;
        Pos existingEntity = createTestPos(id, "Original Name");
        Pos updatedEntity = createTestPos(id, "Updated Name");
        
        when(mockDataService.getById(id)).thenReturn(existingEntity);
        when(mockDataService.upsert(updatedEntity)).thenReturn(updatedEntity);

        // when
        Pos result = crudService.upsert(updatedEntity);

        // then
        assertThat(result).isEqualTo(updatedEntity);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Updated Name");
        verify(mockDataService).getById(id);
        verify(mockDataService).upsert(updatedEntity);
    }

    @Test
    void upsertShouldThrowNotFoundExceptionWhenUpdatingNonExistentEntity() {
        // given
        Long id = 999L;
        Pos entity = createTestPos(id, "Non-existent POS");
        when(mockDataService.getById(id)).thenThrow(new NotFoundException(Pos.class, id));

        // when & then
        assertThrows(NotFoundException.class, () -> crudService.upsert(entity));
        verify(mockDataService).getById(id);
        verify(mockDataService, never()).upsert(any());
    }

    // @Test
    // void upsertShouldThrowDuplicationExceptionWhenConstraintViolated() {
    //     // given
    //     Pos newEntity = createTestPos(null, "Duplicate Name");
    //     when(mockDataService.upsert(newEntity))
    //             .thenThrow(new DuplicationException(Pos.class, "name", "Duplicate Name"));

    //     // when & then
    //     DuplicationException exception = assertThrows(
    //             DuplicationException.class,
    //             () -> crudService.upsert(newEntity)
    //     );
    //     assertThat(exception.getMessage()).contains("Pos");
    //     assertThat(exception.getMessage()).contains("name");
    //     assertThat(exception.getMessage()).contains("Duplicate Name");
    //     verify(mockDataService).upsert(newEntity);
    //     verify(mockDataService, never()).getById(any());
    // }

    // @Test
    // void upsertShouldThrowDuplicationExceptionWhenUpdatingWithDuplicateValue() {
    //     // given
    //     Long id = 1L;
    //     Pos existingEntity = createTestPos(id, "Original Name");
    //     Pos updatedEntity = createTestPos(id, "Duplicate Name");
        
    //     when(mockDataService.getById(id)).thenReturn(existingEntity);
    //     when(mockDataService.upsert(updatedEntity))
    //             .thenThrow(new DuplicationException(Pos.class, "name", "Duplicate Name"));

    //     // when & then
    //     DuplicationException exception = assertThrows(
    //             DuplicationException.class,
    //             () -> crudService.upsert(updatedEntity)
    //     );
    //     assertThat(exception.getMessage()).contains("Pos");
    //     assertThat(exception.getMessage()).contains("Duplicate Name");
    //     verify(mockDataService).getById(id);
    //     verify(mockDataService).upsert(updatedEntity);
    // }

    @Test
    void deleteShouldCallDataServiceDelete() {
        // given
        Long id = 1L;
        doNothing().when(mockDataService).delete(id);

        // when
        crudService.delete(id);

        // then
        verify(mockDataService).delete(id);
    }

    @Test
    void deleteShouldThrowNotFoundExceptionWhenEntityDoesNotExist() {
        // given
        Long id = 999L;
        doThrow(new NotFoundException(Pos.class, id))
                .when(mockDataService).delete(id);

        // when & then
        assertThrows(NotFoundException.class, () -> crudService.delete(id));
        verify(mockDataService).delete(id);
    }

    @Test
    void dataServiceShouldReturnInjectedDataService() {
        // when
        CrudDataService<Pos, Long> result = crudService.dataService();

        // then
        assertThat(result).isEqualTo(mockDataService);
    }
    

    private Pos createTestPos(Long id, String name) {
        return Pos.builder()
                .id(id)
                .name(name)
                .description("Test Description")
                .type(PosType.CAFE)
                .campus(CampusType.ALTSTADT)
                .street("Teststraße")
                .houseNumber("1")
                .postalCode(64823)
                .city("Teststadt")
                .build();
    }


    static class TestPosService extends CrudServiceImpl<Pos, Long> {
        private final CrudDataService<Pos, Long> dataService;

        public TestPosService(CrudDataService<Pos, Long> dataService) {
            super(Pos.class);
            this.dataService = dataService;
        }

        @Override
        protected CrudDataService<Pos, Long> dataService() {
            return dataService;
        }
    }
}
