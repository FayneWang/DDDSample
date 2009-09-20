package se.citerus.dddsample.domain.model.voyage;

import org.apache.commons.lang.Validate;
import se.citerus.dddsample.domain.model.location.Location;
import se.citerus.dddsample.domain.shared.Entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A Voyage.
 */
public class Voyage implements Entity<Voyage> {

  private VoyageNumber voyageNumber;
  private Schedule schedule;

  // Null object pattern
  public static final Voyage NONE = new Voyage(
    new VoyageNumber(""), Schedule.EMPTY
  );

  public Voyage(final VoyageNumber voyageNumber, final Schedule schedule) {
    Validate.notNull(voyageNumber, "Voyage number is required");
    Validate.notNull(schedule, "Schedule is required");

    this.voyageNumber = voyageNumber;
    this.schedule = schedule;
  }

  /**
   * @return Voyage number.
   */
  public VoyageNumber voyageNumber() {
    return voyageNumber;
  }

  /**
   * @return Schedule.
   */
  public Schedule schedule() {
    return schedule;
  }

  /**
   * @param location         location from where the rescheduled departure happens.
   * @param newDepartureTime new departure time
   */
  public void departureRescheduled(final Location location, final Date newDepartureTime) {
    // TODO Change departure/arrival by diffs instead of absolute times

    final int size = schedule.carrierMovements().size();
    final List<CarrierMovement> carrierMovements = new ArrayList<CarrierMovement>(size);

    for (CarrierMovement carrierMovement : schedule.carrierMovements()) {
      if (carrierMovement.departureLocation().sameAs(location)) {
        carrierMovements.add(carrierMovement.withDepartureTime(newDepartureTime));
      } else {
        carrierMovements.add(carrierMovement);
      }
    }

    this.schedule = new Schedule(carrierMovements);
  }

  @Override
  public int hashCode() {
    return voyageNumber.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof Voyage)) return false;

    final Voyage that = (Voyage) o;

    return sameAs(that);
  }

  @Override
  public boolean sameAs(Voyage other) {
    return other != null && this.voyageNumber().sameValueAs(other.voyageNumber());
  }

  @Override
  public String toString() {
    return "Voyage " + voyageNumber;
  }

  Voyage() {
    // Needed by Hibernate
  }

  // Needed by Hibernate
  private Long id;


  /**
   * Builder pattern is used for incremental construction
   * of a Voyage aggregate. This serves as an aggregate factory.
   */
  public static final class Builder {

    private final List<CarrierMovement> carrierMovements = new ArrayList<CarrierMovement>();
    private final VoyageNumber voyageNumber;
    private Location departureLocation;

    public Builder(final VoyageNumber voyageNumber, final Location departureLocation) {
      Validate.notNull(voyageNumber, "Voyage number is required");
      Validate.notNull(departureLocation, "Departure location is required");

      this.voyageNumber = voyageNumber;
      this.departureLocation = departureLocation;
    }

    public Builder addMovement(Location arrivalLocation, Date departureTime, Date arrivalTime) {
      carrierMovements.add(new CarrierMovement(departureLocation, arrivalLocation, departureTime, arrivalTime));
      // Next departure location is the same as this arrival location
      this.departureLocation = arrivalLocation;
      return this;
    }

    public Voyage build() {
      return new Voyage(voyageNumber, new Schedule(carrierMovements));
    }

  }

}
