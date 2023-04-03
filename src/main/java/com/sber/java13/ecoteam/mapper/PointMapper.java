package com.sber.java13.ecoteam.mapper;

import com.sber.java13.ecoteam.dto.PointDTO;
import com.sber.java13.ecoteam.model.GenericModel;
import com.sber.java13.ecoteam.model.Point;
import com.sber.java13.ecoteam.repository.WasteRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PointMapper extends GenericMapper<Point, PointDTO> {
    private final WasteRepository wasteRepository;
    
    public PointMapper(ModelMapper modelMapper, WasteRepository wasteRepository) {
        super(modelMapper, Point.class, PointDTO.class);
        this.wasteRepository = wasteRepository;
    }
    
    @Override
    @PostConstruct
    protected void setupMapper() {
        modelMapper.createTypeMap(Point.class, PointDTO.class)
                .addMappings(m -> m.skip(PointDTO::setWastesIds)).setPostConverter(toDtoConverter());
        modelMapper.createTypeMap(PointDTO.class, Point.class)
                .addMappings(m -> m.skip(Point::setWastes)).setPostConverter(toEntityConverter());
    }
    
    @Override
    protected void mapSpecificFields(PointDTO source, Point destination) {
        if (!Objects.isNull(source.getWastesIds())) {
            destination.setWastes(new HashSet<>(wasteRepository.findAllById(source.getWastesIds())));
        }
        else {
            destination.setWastes(Collections.emptySet());
        }
    }
    
    @Override
    protected void mapSpecificFields(Point source, PointDTO destination) {
        destination.setWastesIds(getIds(source));
    }
    
    @Override
    protected Set<Long> getIds(Point point) {
        return Objects.isNull(point) || Objects.isNull(point.getWastes())
                ? null
                : point.getWastes().stream()
                .map(GenericModel::getId)
                .collect(Collectors.toSet());
    }
}