package com.jjmeg.feeder.datasys.flink.common.util;

import com.google.common.collect.ArrayListMultimap;

import java.util.*;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:22
 */
public class DAG<Vertex> {
    @Override
    public String toString() {
        return "DAG{" +
                "properties=" + properties +
                ", _tasks=" + _tasks +
                ", _dependencies=" + _dependencies +
                ", fixedDependencies=" + fixedDependencies +
                ", _errors=" + _errors +
                '}';
    }

    public int size() {
        return _tasks.size();
    }
    public Map<String, String> properties;
    private final HashSet<Vertex> _tasks = new HashSet<Vertex>();
    public final ArrayListMultimap<Vertex, Vertex> _dependencies = ArrayListMultimap
            .create();

    public final ArrayListMultimap<Vertex, Vertex> fixedDependencies = ArrayListMultimap
            .create();
    private Map<Vertex, Vertex> _errors = null;

    public enum Status {
        /** All tasks were successfully scheduled. */
        COMPLETED_ALL_TASKS,
        /** Some tasks resulted in errors. Check getErrors() for the errors. */
        ERRORS,
        /**
         * The dependencies formed a cycle resulting in some tasks not being
         * able to be scheduled.
         */
        INVALID_DEPENDENCIES
    }

    /**
     * Determines the status of this graph. Call this only after the DAG has
     * been been executed by a DAGExecutor.
     */
    public synchronized Status status() {
        if (_tasks.size() == 0)
            return Status.COMPLETED_ALL_TASKS;
        if (_errors != null)
            return Status.ERRORS;
        if (_tasks.size() > 0)
            return Status.INVALID_DEPENDENCIES;
        throw new RuntimeException("entered unknown state");
    }

    /** Returns a mapping from failed tasks to the exceptions each threw. */
    public synchronized Map<Vertex, Vertex> getErrors() {
        return _errors;
    }

    /** Find the next runnable task, without removing it from _tasks */
    private synchronized Vertex peekNextRunnableTask() {
        for (Vertex t : _tasks) {

            if (_dependencies.containsKey(t)) {
                List<Vertex> v = _dependencies.get(t);

                if (v.isEmpty())
                    return t;
            } else {
                return t;
            }
        }
        return null;
    }

    /**
     * Determine if there is a task that can now be run, because it has no
     * outstanding unfinished dependencies
     */
    public synchronized boolean hasNextRunnableTask() {

        return peekNextRunnableTask() != null;
    }

    /**
     * Determine if there are any remaining tasks in this executor. If
     * hasNextRunnableTask() has returned true, then these remaining tasks
     * cannot be scheduled due to failed dependencies or cycles in the graph.
     */
    public synchronized boolean hasTasks() {
        return _tasks.size() > 0;
    }

    /** Add an in-degree-zero task to this graph. */
    public synchronized void insert(Vertex task) {
        _tasks.add(task);
    }

    /** Add a task that depends upon another specified task to this DAG. **/
    public synchronized void insert(Vertex task, Vertex dependency) {

        _tasks.add(task);
        _dependencies.put(task, dependency);
        fixedDependencies.put(task, dependency);
    }

    /** Add a task that depends upon a set of tasks to this DAG. **/
    public synchronized void insert(Vertex task, Set<Vertex> dependencies) {
        _tasks.add(task);
        _dependencies.putAll(task, dependencies);
        fixedDependencies.putAll(task, dependencies);
    }

    public synchronized Vertex nextRunnableTask() {

        Vertex r = peekNextRunnableTask();
        _tasks.remove(r);
        return r;
    }

    public synchronized void notifyDone(Vertex task) {
        // Remove t from the list of remaining dependencies for any other tasks.
        List removeList = new ArrayList<Vertex>();
        for(Vertex v: _dependencies.values()) {
            //removeList.add(v);
            if(v.equals(task))
                removeList.add(v);
            //_dependencies.values().remove(v);
        }

        _dependencies.values().removeAll(removeList);
    }

    public synchronized void notifyError(Vertex r, Vertex error) {
        _errors.put(r, error);
    }

    public int numTasks() {
        return _tasks.size();
    }

    /**
     * Verify the validity of the DAG, throwing exceptions if invalid
     * dependencies are found.
     */
    public boolean verifyValidGraph(){
        for (Vertex d : _dependencies.values()) {
            if (!_tasks.contains(d)) {
                return false;
            }
        }

        return true;
    }


}
