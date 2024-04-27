package org.students.simplebitcoinnode.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DTOMapperWrapper {
    private final ModelMapper mapper = new ModelMapper();

    /**
     * This method is used to convert a list of source objects to a list of destination objects.
     * It uses Java streams to map each source object to a destination object.
     *
     * @param <S> The type of the source objects.
     * @param <D> The type of the destination objects.
     * @param source The list of source objects to be converted.
     * @param destination The class of the destination objects.
     * @return A list of destination objects.
     */
    public <S, D> List<D> mapAll(final List<S> source, Class<D> destination){
        return source.stream().map(entity -> mapper.map(entity, destination)).collect(Collectors.toList());
    }

    /**
     * This method is used to convert a source object to a destination object.
     *
     * @param <S> The type of the source object.
     * @param <D> The type of the destination object.
     * @param source The source object to be converted.
     * @param destination The class of the destination object.
     * @return A destination object.
     */
    public <S, D> D map(final S source, Class<D> destination){
        return mapper.map(source, destination);
    }

    /**
     * This method is used to convert a destination object back to a source object.
     *
     * @param <S> The type of the source object.
     * @param <D> The type of the destination object.
     * @param destination The destination object to be converted.
     * @param source The class of the source object.
     * @return A source object.
     */
    public <S, D> S unmap(final D destination, Class<S> source){
        return mapper.map(destination, source);
    }

}
