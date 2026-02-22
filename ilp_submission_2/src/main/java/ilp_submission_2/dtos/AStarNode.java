package ilp_submission_2.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AStarNode implements Comparable<AStarNode> {
    private final Point point;
    private double gCost;
    private double hCost;
    private AStarNode parent;

    public AStarNode(Point point) {
        this.point = point;
    }

    public double getFCost() {
        return gCost + hCost;
    }

    @Override
    public int compareTo(AStarNode other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AStarNode)) return false;
        return this.point.equals(((AStarNode) obj).point);
    }

    @Override
    public int hashCode() {
        return point.hashCode();
    }

}
